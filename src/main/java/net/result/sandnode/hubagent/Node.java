package net.result.sandnode.hubagent;

import net.result.sandnode.chain.ServerChainManager;
import net.result.sandnode.encryption.Encryptions;
import net.result.sandnode.encryption.KeyStorageRegistry;
import net.result.sandnode.message.Message;
import net.result.sandnode.message.util.NodeType;
import net.result.sandnode.serverclient.Peer;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

public abstract class Node {
    public final KeyStorageRegistry keyStorageRegistry;
    public final List<BiConsumer<Peer, Message>> consumers = new ArrayList<>();

    public Node(@NotNull KeyStorageRegistry keyStorageRegistry) {
        this.keyStorageRegistry = keyStorageRegistry;
        addSendingHandler(Node::defaultHandler);
    }

    private static void defaultHandler(Peer peer, Message message) {
        var io = peer.io();
        var headers = message.headers();
        headers.setConnection(io.connection);
        if (message.headersEncryption() == Encryptions.NONE) {
            message.setHeadersEncryption(io.currentEncryption());
        }
        if (headers.bodyEncryption() == Encryptions.NONE) {
            headers.setBodyEncryption(io.currentEncryption());
        }
    }

    public Agent agent() {
        return (Agent) this;
    }

    public Hub hub() {
        return (Hub) this;
    }

    public void addSendingHandler(BiConsumer<Peer, Message> consumer) {
        consumers.add(consumer);
    }

    public abstract @NotNull ServerChainManager createChainManager();

    public abstract @NotNull NodeType type();

    public void beforeSending(Peer peer, Message message) {
        consumers.forEach(consumer -> consumer.accept(peer, message));
    }

    @SuppressWarnings("EmptyMethod")
    public void close() {}

    @Override
    public String toString() {
        return "<%s %s>".formatted(getClass().getSimpleName(), keyStorageRegistry);
    }
}
