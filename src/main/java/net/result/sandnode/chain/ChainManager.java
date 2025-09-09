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

    @SuppressWarnings("unused")
    Optional<Chain> getChain(String chainName);

    <T extends Chain> T getChain(String chainName, Supplier<T> orElse);

    void setName(short chainID, String chainName);
}
