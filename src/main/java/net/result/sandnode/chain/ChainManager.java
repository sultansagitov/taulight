package net.result.sandnode.chain;

import net.result.sandnode.exception.BusyChainID;
import net.result.sandnode.message.RawMessage;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

public interface ChainManager {
    void interruptAll();

    Optional<Chain> getByID(short id);

    void linkChain(Chain chain);

    Chain createNew(RawMessage message) throws BusyChainID;

    Chain defaultChain(RawMessage message);

    void removeChain(Chain chain);

    void distributeMessage(RawMessage message) throws InterruptedException;

    Set<Chain> getAllChains();

    Map<String, Chain> getChainsMap();

    Optional<Chain> getChain(String chainName);

    void setName(Chain chain, String chainName);
}
