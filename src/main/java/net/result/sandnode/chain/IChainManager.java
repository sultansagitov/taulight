package net.result.sandnode.chain;

import net.result.sandnode.exceptions.BSTBusyPosition;
import net.result.sandnode.messages.RawMessage;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

public interface IChainManager {
    Optional<IChain> getByID(short id);

    void addChain(IChain chain);

    IChain createNew(RawMessage message) throws BSTBusyPosition;

    IChain defaultChain(RawMessage message);

    void removeChain(IChain chain);

    void distributeMessage(RawMessage message) throws InterruptedException;

    Set<IChain> getAllChains();

    Map<String, IChain> getChainsMap();

    Optional<IChain> getChain(String contextName);

    void removeChain(String contextName);
}
