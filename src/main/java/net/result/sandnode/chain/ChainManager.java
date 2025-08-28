package net.result.sandnode.chain;

import net.result.sandnode.message.RawMessage;
import net.result.sandnode.message.util.MessageType;

import java.util.Optional;
import java.util.function.Supplier;

public interface ChainManager {
    void interruptAll();

    void addHandler(MessageType type, Supplier<ReceiverChain> supplier);

    ChainStorage storage();

    void linkChain(Chain chain);

    void removeChain(Chain chain);

    void distributeMessage(RawMessage message);

    Optional<Chain> getChain(String chainName);

    void setName(Chain chain, String chainName);
}
