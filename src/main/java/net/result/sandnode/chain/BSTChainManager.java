package net.result.sandnode.chain;

import net.result.sandnode.bst.AVLTree;
import net.result.sandnode.exceptions.BSTBusyPosition;
import net.result.sandnode.exceptions.ImpossibleRuntimeException;
import net.result.sandnode.messages.RawMessage;
import net.result.sandnode.bst.BinarySearchTree;
import net.result.sandnode.messages.util.Headers;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.security.SecureRandom;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static net.result.sandnode.messages.util.MessageTypes.CHAIN_NAME;

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
        Chain chain = getByID(headers.getChainID()).orElseGet(() -> {
                    try {
                        Chain aNew = createNew(message);
                        if (aNew.isChainStartAllowed()) aNew.async(executorService);
                        return aNew;
                    } catch (BSTBusyPosition e) {
                        throw new ImpossibleRuntimeException(e);
                    }
                });

        if (headers.getType() == CHAIN_NAME) {
            headers.getOptionalValue("chain-name").ifPresent(s -> setName(chain, s));
        } else {
            chain.put(message);
        }
    }

    @Override
    public Set<Chain> getAllChains() {
        return new HashSet<>(bst.getOrdered());
    }

    @Override
    public Map<String, Chain> getChainsMap() {
        return chainMap;
    }

    @Override
    public Optional<Chain> getChain(String contextName) {
        return Optional.ofNullable(chainMap.get(contextName));
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
