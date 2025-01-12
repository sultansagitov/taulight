package net.result.sandnode.chain;

import net.result.sandnode.exceptions.BSTBusyPosition;
import net.result.sandnode.messages.RawMessage;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

public interface ChainManager {
    void interruptAll();

    Optional<Chain> getByID(short id);

    void linkChain(Chain chain);

    Chain createNew(RawMessage message) throws BSTBusyPosition;

    Chain defaultChain(RawMessage message);

    void removeChain(Chain chain);

    void distributeMessage(RawMessage message) throws InterruptedException;

    Set<Chain> getAllChains();

    Map<String, Chain> getChainsMap();

    Optional<Chain> getChain(String contextName);

    void setName(Chain chain, String chainName);
}
