package net.result.sandnode.chain;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;

public interface ChainStorage {
    Optional<Chain> find(short id);

    void add(Chain chain);

    Collection<Chain> getAll();

    void remove(Chain chain);

    void addNamed(String chainName, short chainID);

    Optional<Chain> find(String chainName);

    Map<String, Chain> getNamed();
}
