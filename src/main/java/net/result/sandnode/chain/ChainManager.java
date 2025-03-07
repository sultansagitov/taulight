package net.result.sandnode.chain;

import net.result.sandnode.exception.BusyChainID;
import net.result.sandnode.message.RawMessage;
import net.result.sandnode.message.util.MessageType;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;

public interface ChainManager {
    void interruptAll();

    Optional<IChain> getByID(short id);

    void linkChain(IChain chain);

    ReceiverChain createNew(RawMessage message) throws BusyChainID;

    ReceiverChain createChain(MessageType type);

    void removeChain(IChain chain);

    void distributeMessage(RawMessage message) throws InterruptedException;

    Collection<IChain> getAllChains();

    Map<String, IChain> getChainsMap();

    Optional<IChain> getChain(String chainName);

    void setName(IChain chain, String chainName);

    ExecutorService getExecutorService();
}
