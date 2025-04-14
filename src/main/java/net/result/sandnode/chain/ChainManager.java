package net.result.sandnode.chain;

import net.result.sandnode.message.RawMessage;
import net.result.sandnode.message.util.MessageType;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;

public interface ChainManager {
    void interruptAll();

    void linkChain(IChain chain);

    ReceiverChain createChain(MessageType type);

    void removeChain(IChain chain);

    void distributeMessage(RawMessage message) throws InterruptedException;

    Collection<IChain> getAllChains();

    Map<String, IChain> getChainsMap();

    Optional<IChain> getChain(String chainName);

    void setName(IChain chain, String chainName);

    ExecutorService getExecutorService();
}
