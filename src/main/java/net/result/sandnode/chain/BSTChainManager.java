package net.result.sandnode.chain;

import net.result.sandnode.bst.AVLTree;
import net.result.sandnode.exceptions.BSTBusyPosition;
import net.result.sandnode.exceptions.ImpossibleRuntimeException;
import net.result.sandnode.messages.RawMessage;
import net.result.sandnode.bst.BinarySearchTree;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.security.SecureRandom;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
    public void addChain(Chain chain) {
        List<Short> list = bst.getOrdered().stream().map(Chain::getID).toList();

        LOGGER.info("Randomizing chain id");
        Random random = new SecureRandom();
        short chainID = -1;
        while (list.contains(chainID) || chainID == -1) {
            chainID = (short) random.nextInt(Short.MIN_VALUE, Short.MAX_VALUE);
        }
        chain.setID(chainID);
        chain.setManager(this);
        LOGGER.info("Adding new chain {}-{}", chain.getID(), chain.getClass().getSimpleName());
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
                removeChain(entry.getKey());
            }
        }

        bst.remove(chain);
    }

    @Override
    public void distributeMessage(RawMessage message) throws InterruptedException {
        getByID(message.getHeaders().getChainID())
                .orElseGet(() -> {
                    try {
                        Chain aNew = createNew(message);
                        if (aNew.isChainStartAllowed()) aNew.async(executorService);
                        return aNew;
                    } catch (BSTBusyPosition e) {
                        throw new ImpossibleRuntimeException(e);
                    }
                })
                .put(message);
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
    public void removeChain(String contextName) {
        chainMap.remove(contextName);
    }

    @Override
    public void interruptAll() {
        executorService.shutdownNow();
    }

    @Override
    public String toString() {
        List<String> list = new ArrayList<>();
        for (Chain chain : bst.getOrdered())
            list.add(String.valueOf(chain.getID()));
        return "<%s chains=%s>".formatted(
                getClass().getSimpleName(),
                String.join(",", list)
        );
    }
}
