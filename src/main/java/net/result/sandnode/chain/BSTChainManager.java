package net.result.sandnode.chain;

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

import static net.result.sandnode.message.util.MessageTypes.CHAIN_NAME;

public abstract class BSTChainManager implements ChainManager {
    private static final Logger LOGGER = LogManager.getLogger(BSTChainManager.class);
    protected final BinarySearchTree<Chain, Short> bst = new AVLTree<>();
    protected final Map<String, Chain> chainMap;
    private final ExecutorService executorService = Executors.newCachedThreadPool();

    protected BSTChainManager() {
        this.chainMap = new HashMap<>();
    }

    @Override
    public Optional<Chain> getByID(short id) {
        return bst.find(id);
    }

    @Override
    public void linkChain(Chain chain) {
        List<Short> list = bst.getOrdered().stream().map(Chain::getID).toList();

        Random random = new SecureRandom();
        short chainID;
        do {
            chainID = (short) random.nextInt(Short.MIN_VALUE, Short.MAX_VALUE);
        } while (list.contains(chainID));
        chain.setID(chainID);
        LOGGER.info("Adding new chain {}", chain);
        try {
            bst.add(chain);
        } catch (BSTBusyPosition e) {
            throw new ImpossibleRuntimeException(e);
        }
    }

    @Override
    public void removeChain(Chain chain) {
        for (Map.Entry<String, Chain> entry : chainMap.entrySet()) {
            if (entry.getValue() == chain) {
                chainMap.remove(entry.getKey());
            }
        }

        bst.remove(chain);
    }

    @Override
    public void distributeMessage(RawMessage message) throws InterruptedException {
        Headers headers = message.getHeaders();
        Optional<Chain> opt = getByID(headers.getChainID());
        Chain chain;
        if (opt.isPresent()) {
            chain = opt.get();
        } else {
            try {
                Chain aNew = createNew(message);
                if (aNew.isChainStartAllowed()) aNew.async(executorService);
                chain = aNew;
            } catch (BusyChainID e) {
                throw new RuntimeException(e);
            }
        }

        if (headers.getType() == CHAIN_NAME) {
            headers.getOptionalValue("chain-name").ifPresent(s -> setName(chain, s));
            return;
        }

        chain.put(message);
    }

    @Override
    public Collection<Chain> getAllChains() {
        return new HashSet<>(bst.getOrdered());
    }

    @Override
    public Map<String, Chain> getChainsMap() {
        return chainMap;
    }

    @Override
    public Optional<Chain> getChain(String chainName) {
        return Optional.ofNullable(chainMap.get(chainName));
    }

    @Override
    public void interruptAll() {
        executorService.shutdownNow();
    }

    @Override
    public void setName(Chain chain, String chainName) {
        chainMap.put(chainName, chain);
    }

    @Override
    public String toString() {
        List<String> list = new ArrayList<>();
        for (Chain chain : bst.getOrdered())
            list.add(String.format("%04X", chain.getID()));
        return "<%s chains=%s>".formatted(
                getClass().getSimpleName(),
                String.join(",", list)
        );
    }
}
