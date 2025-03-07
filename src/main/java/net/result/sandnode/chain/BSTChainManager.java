package net.result.sandnode.chain;

import net.result.sandnode.message.util.MessageType;
import net.result.sandnode.message.util.MessageTypes;
import net.result.sandnode.util.bst.AVLTree;
import net.result.sandnode.exception.BSTBusyPosition;
import net.result.sandnode.exception.BusyChainID;
import net.result.sandnode.exception.ImpossibleRuntimeException;
import net.result.sandnode.message.RawMessage;
import net.result.sandnode.util.bst.BinarySearchTree;
import net.result.sandnode.message.util.Headers;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.security.SecureRandom;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class BSTChainManager implements ChainManager {
    private static final Logger LOGGER = LogManager.getLogger(BSTChainManager.class);
    protected final BinarySearchTree<IChain, Short> bst = new AVLTree<>();
    protected final Map<String, IChain> chainMap;
    private final ExecutorService executorService = Executors.newCachedThreadPool();

    protected BSTChainManager() {
        this.chainMap = new HashMap<>();
    }

    @Override
    public Optional<IChain> getByID(short id) {
        return bst.find(id);
    }

    @Override
    public ReceiverChain createNew(RawMessage message) throws BusyChainID {
        Headers headers = message.headers();
        MessageType type = headers.type();
        ReceiverChain chain = createChain(type);

        chain.setID(headers.chainID());
        LOGGER.info("Adding new chain {}", chain);
        try {
            bst.add(chain);
        } catch (BSTBusyPosition e) {
            throw new BusyChainID(e);
        }
        return chain;
    }

    @Override
    public void linkChain(IChain chain) {
        List<Short> list = bst.getOrdered().stream().map(IChain::getID).toList();

        Random random = new SecureRandom();
        short chainID;
        do {
            chainID = (short) random.nextInt(Short.MIN_VALUE, Short.MAX_VALUE);
        } while (list.contains(chainID));
        chain.setID(chainID);
        LOGGER.info("Linking new chain {}", chain);
        try {
            bst.add(chain);
        } catch (BSTBusyPosition e) {
            throw new ImpossibleRuntimeException(e);
        }
    }

    @Override
    public void removeChain(IChain chain) {
        chainMap.entrySet().removeIf(entry -> entry.getValue() == chain);
        bst.remove(chain);
    }

    @Override
    public void distributeMessage(RawMessage message) throws InterruptedException {
        Headers headers = message.headers();
        Optional<IChain> opt = getByID(headers.chainID());
        IChain chain;
        if (opt.isPresent()) {
            chain = opt.get();
        } else {
            try {
                ReceiverChain aNew = createNew(message);
                aNew.async(this);
                chain = aNew;
            } catch (BusyChainID e) {
                throw new RuntimeException(e);
            }
        }

        if (headers.type() == MessageTypes.CHAIN_NAME) {
            headers.getOptionalValue("chain-name").ifPresent(s -> setName(chain, s));
            return;
        }

        chain.put(message);
    }

    @Override
    public Collection<IChain> getAllChains() {
        return new HashSet<>(bst.getOrdered());
    }

    @Override
    public Map<String, IChain> getChainsMap() {
        return chainMap;
    }

    @Override
    public Optional<IChain> getChain(String chainName) {
        return Optional.ofNullable(chainMap.get(chainName));
    }

    @Override
    public void interruptAll() {
        executorService.shutdownNow();
    }

    @Override
    public void setName(IChain chain, String chainName) {
        chainMap.put(chainName, chain);
    }

    @Override
    public ExecutorService getExecutorService() {
        return executorService;
    }

    @Override
    public String toString() {
        Collection<String> list = new ArrayList<>();
        for (IChain chain : bst.getOrdered())
            list.add(String.format("%04X", chain.getID()));
        return "<%s chains=%s>".formatted(getClass().getSimpleName(), String.join(",", list));
    }
}
