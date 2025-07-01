package net.result.sandnode.chain;

import net.result.sandnode.message.RawMessage;
import net.result.sandnode.message.util.MessageType;

import java.util.Optional;

public interface ChainManager {
    void interruptAll();

    ChainStorage storage();

    void linkChain(Chain chain);

    ReceiverChain createChain(MessageType type);

    void removeChain(Chain chain);

    void distributeMessage(RawMessage message) throws InterruptedException;

    Optional<Chain> getChain(String chainName);

    void setName(Chain chain, String chainName);
}
