package net.result.taulight.chain.sender;

import net.result.sandnode.chain.sender.ClientChain;
import net.result.sandnode.error.ServerErrorManager;
import net.result.sandnode.exception.*;
import net.result.sandnode.exception.error.SandnodeErrorException;
import net.result.sandnode.message.RawMessage;
import net.result.sandnode.util.IOController;
import net.result.taulight.dto.ChatInfoDTO;
import net.result.taulight.dto.ChatInfoPropDTO;
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

    public synchronized Optional<Collection<ChatInfoDTO>> getByMember(Collection<ChatInfoPropDTO> infoProps)
            throws InterruptedException, DeserializationException, ExpectedMessageException, SandnodeErrorException,
            UnknownSandnodeErrorException, UnprocessedMessagesException {
        lock.lock();
        try {
            send(ChatRequest.getByMember(infoProps));
            RawMessage raw = queue.take();

            ServerErrorManager.instance().handleError(raw);

            return Optional.of(new ChatResponse(raw).getInfos());
        } finally {
            lock.unlock();
        }
    }

    public synchronized Collection<ChatInfoDTO> getByID(Collection<UUID> chatID, Collection<ChatInfoPropDTO> infoProps)
            throws InterruptedException, ExpectedMessageException, UnknownSandnodeErrorException,
            SandnodeErrorException, DeserializationException, UnprocessedMessagesException {
        lock.lock();
        try {
            send(ChatRequest.getByID(chatID, infoProps));
            RawMessage raw = queue.take();

            ServerErrorManager.instance().handleError(raw);

            ChatResponse chatResponse = new ChatResponse(raw);
            return chatResponse.getInfos();
        } finally {
            lock.unlock();
        }
    }
}
