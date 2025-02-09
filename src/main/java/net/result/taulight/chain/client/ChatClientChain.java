package net.result.taulight.chain.client;

import net.result.sandnode.chain.client.ClientChain;
import net.result.sandnode.exception.DeserializationException;
import net.result.sandnode.exception.ExpectedMessageException;
import net.result.sandnode.exception.ImpossibleRuntimeException;
import net.result.sandnode.message.RawMessage;
import net.result.sandnode.message.types.ErrorMessage;
import net.result.sandnode.error.SandnodeError;
import net.result.sandnode.message.util.MessageTypes;
import net.result.sandnode.util.IOController;
import net.result.taulight.message.types.ChatRequest;
import net.result.taulight.message.types.ChatResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ChatClientChain extends ClientChain {
    private static final Logger LOGGER = LogManager.getLogger(ChatClientChain.class);
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

    private static void handleError(RawMessage raw) throws DeserializationException {
        SandnodeError error = new ErrorMessage(raw).error;
        LOGGER.error("Error Code: {}, Error Description: {}", error.getCode(), error.getDescription());
    }

    public Optional<Collection<String>> getChats()
            throws InterruptedException, DeserializationException, ExpectedMessageException {
        lock.lock();
        try {
            send(new ChatRequest(ChatRequest.DataType.GET));
            RawMessage raw = queue.take();

            if (raw.getHeaders().getType() == MessageTypes.ERR) {
                handleError(raw);
                return Optional.empty();
            }

            Collection<String> chats = new ChatResponse(raw).getChats();
            return Optional.of(chats);
        } finally {
            lock.unlock();
        }
    }
}
