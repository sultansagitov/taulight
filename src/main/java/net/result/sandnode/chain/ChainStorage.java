package net.result.sandnode.chain;

import net.result.sandnode.exception.BusyChainID;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

public interface ChainStorage {
    Optional<Chain> find(short id);

    void add(Chain chain) throws BusyChainID;

    Collection<Chain> getAll();

    void remove(Chain chain);

    void addNamed(String chainName, Chain chain);

    Optional<Chain> find(String chainName);

    Map<String, Chain> getNamed();
}
