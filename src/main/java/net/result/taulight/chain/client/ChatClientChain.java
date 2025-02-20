package net.result.taulight.chain.client;

import net.result.sandnode.chain.client.ClientChain;
import net.result.sandnode.error.ServerErrorManager;
import net.result.sandnode.exception.*;
import net.result.sandnode.message.RawMessage;
import net.result.sandnode.message.types.ErrorMessage;
import net.result.sandnode.message.util.MessageTypes;
import net.result.sandnode.util.IOController;
import net.result.taulight.message.ChatInfo;
import net.result.taulight.message.ChatInfoProp;
import net.result.taulight.message.types.ChatRequest;
import net.result.taulight.message.types.ChatResponse;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ChatClientChain extends ClientChain {
    private final Lock lock = new ReentrantLock();

    public ChatClientChain(IOController io) {
        super(io);
    }

    @Override
    public boolean isChainStartAllowed() {
        return false;
    }

    @Override
    public void sync() {
        throw new ImpossibleRuntimeException("This chain should not be started");
    }

    public Optional<Collection<ChatInfo>> getByMember(Collection<ChatInfoProp> infoProps)
            throws InterruptedException, DeserializationException, ExpectedMessageException, SandnodeErrorException,
            UnknownSandnodeErrorException {
        lock.lock();
        try {
            send(ChatRequest.getByMember(infoProps));
            RawMessage raw = queue.take();

            if (raw.headers().type() == MessageTypes.ERR) {
                ErrorMessage errorMessage = new ErrorMessage(raw);
                ServerErrorManager.instance().throwAll(errorMessage.error);
                return Optional.empty();
            }

            return Optional.of(new ChatResponse(raw).getInfos());
        } finally {
            lock.unlock();
        }
    }
    public Optional<Collection<ChatInfo>> getByID(Collection<UUID> chatID, Collection<ChatInfoProp> infoProps)
            throws InterruptedException, ExpectedMessageException, UnknownSandnodeErrorException,
            SandnodeErrorException, DeserializationException {
        lock.lock();
        try {
            send(ChatRequest.getByID(chatID, infoProps));
            RawMessage raw = queue.take();

            if (raw.headers().type() == MessageTypes.ERR) {
                ErrorMessage errorMessage = new ErrorMessage(raw);
                ServerErrorManager.instance().throwAll(errorMessage.error);
                return Optional.empty();
            }

            ChatResponse chatResponse = new ChatResponse(raw);
            return Optional.of(chatResponse.getInfos());
        } finally {
            lock.unlock();
        }
    }
}
