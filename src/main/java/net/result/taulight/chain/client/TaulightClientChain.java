package net.result.taulight.chain.client;

import net.result.sandnode.chain.client.ClientChain;
import net.result.sandnode.exceptions.DeserializationException;
import net.result.sandnode.exceptions.ExpectedMessageException;
import net.result.sandnode.exceptions.ImpossibleRuntimeException;
import net.result.sandnode.messages.RawMessage;
import net.result.sandnode.messages.types.ErrorMessage;
import net.result.sandnode.server.ServerErrorInterface;
import net.result.sandnode.util.IOControl;
import net.result.taulight.messages.types.TaulightRequestMessage;
import net.result.taulight.messages.DataType;
import net.result.taulight.messages.types.TaulightResponseMessage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Optional;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static net.result.sandnode.messages.util.MessageTypes.ERR;

public class TaulightClientChain extends ClientChain {
    private static final Logger LOGGER = LogManager.getLogger(TaulightClientChain.class);
    private final Lock lock = new ReentrantLock();

    public TaulightClientChain(IOControl io) {
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
        ServerErrorInterface error = new ErrorMessage(raw).error;
        LOGGER.error("Error Code: {}, Error Description: {}", error.getCode(), error.getDescription());
    }

    public Optional<Set<String>> getChats()
            throws InterruptedException, DeserializationException, ExpectedMessageException {
        lock.lock();
        try {
            send(new TaulightRequestMessage(DataType.GET));
            RawMessage raw = queue.take();

            if (raw.getHeaders().getType() == ERR) {
                handleError(raw);
                return Optional.empty();
            }

            Set<String> chats = new TaulightResponseMessage(raw).getChats();
            return Optional.of(chats);
        } finally {
            lock.unlock();
        }
    }

    public void addToGroup(String group) throws InterruptedException, DeserializationException {
        lock.lock();
        try {
            send(new TaulightRequestMessage(TaulightRequestMessage.TaulightRequestData.addGroup(group)));
            RawMessage raw = queue.take();

            if (raw.getHeaders().getType() == ERR) {
                handleError(raw);
            }
        } finally {
            lock.unlock();
        }
    }

    public void write(String group, String message) throws InterruptedException, DeserializationException {
        lock.lock();
        try {
            send(new TaulightRequestMessage(TaulightRequestMessage.TaulightRequestData.write(group, message)));
            RawMessage raw = queue.take();

            if (raw.getHeaders().getType() == ERR) {
                handleError(raw);
            }
        } finally {
            lock.unlock();
        }
    }
}
