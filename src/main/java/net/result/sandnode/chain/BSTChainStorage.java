package net.result.sandnode.chain;

import net.result.sandnode.exception.BSTBusyPosition;
import net.result.sandnode.exception.BusyChainID;
import net.result.sandnode.util.bst.BinarySearchTree;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class BSTChainStorage implements ChainStorage {
    private final BinarySearchTree<Chain, Short> bst;
    private final Map<String, Chain> chainMap = new HashMap<>();

    public BSTChainStorage(BinarySearchTree<Chain, Short> bst) {
        this.bst = bst;
    }

    @Override
    public Optional<Chain> find(String chainName) {
        return Optional.ofNullable(chainMap.get(chainName));
    }

    @Override
    public Optional<Chain> find(short id) {
        return bst.find(id);
    }

    @Override
    public void addNamed(String chainName, Chain chain) {
        chainMap.put(chainName, chain);
    }

    @Override
    public void add(Chain chain) throws BusyChainID {
        try {
            bst.add(chain);
        } catch (BSTBusyPosition e) {
            throw new BusyChainID(e);
        }
    }

    @Override
    public Collection<Chain> getAll() {
        return bst.getOrdered();
    }

    @Override
    public Map<String, Chain> getNamed() {
        return chainMap;
    }

    @Override
    public void remove(Chain chain) {
        chainMap.entrySet().removeIf(entry -> entry.getValue() == chain);
        bst.remove(chain);
    }
}
