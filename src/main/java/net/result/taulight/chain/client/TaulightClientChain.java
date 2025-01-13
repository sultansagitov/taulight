package net.result.taulight.chain.client;

import net.result.sandnode.chain.client.ClientChain;
import net.result.sandnode.exceptions.DeserializationException;
import net.result.sandnode.exceptions.ExpectedMessageException;
import net.result.sandnode.exceptions.ImpossibleRuntimeException;
import net.result.sandnode.messages.RawMessage;
import net.result.sandnode.messages.types.ErrorMessage;
import net.result.sandnode.server.ServerError;
import net.result.sandnode.util.IOControl;
import net.result.taulight.messages.types.TaulightRequestMessage;
import net.result.taulight.messages.DataType;
import net.result.taulight.messages.types.TaulightResponseMessage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Optional;
import java.util.Set;

import static net.result.sandnode.messages.util.MessageTypes.ERR;

public class TaulightClientChain extends ClientChain {
    private static final Logger LOGGER = LogManager.getLogger(TaulightClientChain.class);

    public TaulightClientChain(IOControl io) {
        super(io);
    }

    @Override
    public boolean isChainStartAllowed() {
        return false;
    }

    @Override
    public void sync() {
        throw new ImpossibleRuntimeException("This chain should no be started");
    }

    public Optional<Set<String>> getChats() throws InterruptedException, DeserializationException, ExpectedMessageException {
        send(new TaulightRequestMessage(DataType.GET));
        RawMessage raw = queue.take();

        if (raw.getHeaders().getType() == ERR) {
            ServerError error = new ErrorMessage(raw).error;
            LOGGER.error("Error Code: {}, Error Description: {}", error.code, error.desc);
            return Optional.empty();
        }

        Set<String> chats = new TaulightResponseMessage(raw).getChats();
        return Optional.of(chats);
    }

    @Deprecated(forRemoval = true)
    public void addToGroup(String group) throws InterruptedException, DeserializationException {
        send(new TaulightRequestMessage(TaulightRequestMessage.TaulightRequestData.addGroup(group)));
        RawMessage raw = queue.take();

        if (raw.getHeaders().getType() == ERR) {
            ServerError error = new ErrorMessage(raw).error;
            LOGGER.error("Error code: {} description: {}", error.code, error.desc);
        }
    }

    @Deprecated(forRemoval = true)
    public void write(String group, String message) throws InterruptedException, DeserializationException {
        send(new TaulightRequestMessage(TaulightRequestMessage.TaulightRequestData.write(group, message)));
        RawMessage raw = queue.take();

        if (raw.getHeaders().getType() == ERR) {
            ServerError error = new ErrorMessage(raw).error;
            LOGGER.error("Error code: {} description: {}", error.code, error.desc);
        }
    }
}
