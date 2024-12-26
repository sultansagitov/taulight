package net.result.sandnode.chain;

import net.result.sandnode.exceptions.BSTBusyPosition;
import net.result.sandnode.exceptions.ImpossibleRuntimeException;
import net.result.sandnode.messages.RawMessage;
import net.result.sandnode.util.bst.BinarySearchTree;
import net.result.sandnode.util.bst.IBinarySearchTree;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.security.SecureRandom;
import java.util.*;


public abstract class BSTChainManager implements IChainManager {
    private static final Logger LOGGER = LogManager.getLogger(BSTChainManager.class);
    protected final IBinarySearchTree<IChain, Short> bst = new BinarySearchTree<>();
    protected final Map<String, IChain> chainMap;

    protected BSTChainManager() {
        this.chainMap = new HashMap<>();
    }

    @Override
    public Optional<IChain> getByID(short id) {
        return bst.find(id);
    }

    @Override
    public void addChain(IChain chain) {
        List<Short> list = bst.getOrdered().stream().map(IChain::getID).toList();
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
    public void removeChain(IChain chain) {
        for (Map.Entry<String, IChain> entry : chainMap.entrySet()) {
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
                        IChain aNew = createNew(message);
                        if (aNew.isChainStartAllowed()) aNew.async();
                        return aNew;
                    } catch (BSTBusyPosition e) {
                        throw new ImpossibleRuntimeException(e);
                    }
                })
                .put(message);
    }

    @Override
    public Set<IChain> getAllChains() {
        return new HashSet<>(bst.getOrdered());
    }

    @Override
    public Map<String, IChain> getChainsMap() {
        return chainMap;
    }

    @Override
    public Optional<IChain> getChain(String contextName) {
        return Optional.ofNullable(chainMap.get(contextName));
    }

    @Override
    public void removeChain(String contextName) {
        chainMap.remove(contextName);
    }

    @Override
    public String toString() {
        List<String> list = new ArrayList<>();
        for (IChain chain : bst.getOrdered())
            list.add(String.valueOf(chain.getID()));
        return "<%s chains=%s>".formatted(
                getClass().getSimpleName(),
                String.join(",", list)
        );
    }
}
