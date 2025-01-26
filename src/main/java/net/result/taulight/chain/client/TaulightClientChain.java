package net.result.taulight.chain.client;

import net.result.sandnode.chain.client.ClientChain;
import net.result.sandnode.exception.DeserializationException;
import net.result.sandnode.exception.ExpectedMessageException;
import net.result.sandnode.exception.ImpossibleRuntimeException;
import net.result.sandnode.message.RawMessage;
import net.result.sandnode.message.types.ErrorMessage;
import net.result.sandnode.error.SandnodeError;
import net.result.sandnode.util.IOController;
import net.result.taulight.message.types.TaulightRequestMessage;
import net.result.taulight.message.DataType;
import net.result.taulight.message.types.TaulightResponseMessage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static net.result.sandnode.message.util.MessageTypes.ERR;

public class TaulightClientChain extends ClientChain {
    private static final Logger LOGGER = LogManager.getLogger(TaulightClientChain.class);
    private final Lock lock = new ReentrantLock();

    public TaulightClientChain(IOController io) {
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
            send(new TaulightRequestMessage(DataType.GET));
            RawMessage raw = queue.take();

            if (raw.getHeaders().getType() == ERR) {
                handleError(raw);
                return Optional.empty();
            }

            Collection<String> chats = new TaulightResponseMessage(raw).getChats();
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
}
