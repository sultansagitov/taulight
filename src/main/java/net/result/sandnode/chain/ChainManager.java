package net.result.sandnode.chain;

import net.result.sandnode.exception.BusyChainID;
import net.result.sandnode.message.RawMessage;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;

public interface ChainManager {
    void interruptAll();

    Optional<Chain> getByID(short id);

    void linkChain(Chain chain);

    Chain createNew(RawMessage message) throws BusyChainID;

    Chain defaultChain(RawMessage message);

    void removeChain(Chain chain);

    void distributeMessage(RawMessage message) throws InterruptedException;

    Collection<Chain> getAllChains();

    Map<String, Chain> getChainsMap();

    Optional<Chain> getChain(String chainName);

    void setName(Chain chain, String chainName);

    ExecutorService getExecutorService();
}
