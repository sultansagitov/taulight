package net.result.sandnode.error;

import net.result.sandnode.exception.ExpectedMessageException;
import net.result.sandnode.exception.ImpossibleRuntimeException;
import net.result.sandnode.exception.UnknownSandnodeErrorException;
import net.result.sandnode.exception.error.SandnodeErrorException;
import net.result.sandnode.message.RawMessage;
import net.result.sandnode.message.types.ErrorMessage;
import net.result.sandnode.message.util.MessageTypes;
import net.result.sandnode.util.Manager;

import java.util.*;

public class ServerErrorManager extends Manager<SandnodeError> {
    public interface ThrowHandler {
        @SuppressWarnings("unused")
        void throwFunc(SandnodeError error) throws SandnodeErrorException;
    }

    private final Collection<ThrowHandler> throwHandlers = new ArrayList<>();

    private static final ServerErrorManager INSTANCE = new ServerErrorManager();
    private ServerErrorManager() {}
    public static ServerErrorManager instance() {
        return INSTANCE;
    }
    static {
        ServerErrorManager instance = instance();
        Arrays.stream(Errors.values()).forEach(instance::add);
        instance.addThrowHandler(Errors::throwHandler);
    }

    @Override
    protected void handleOverflow(SandnodeError error) {
        list.removeIf(e -> e.code() == error.code());
    }

    public void addThrowHandler(ThrowHandler handler) {
        throwHandlers.add(handler);
    }

    public void throwAll(SandnodeError error) throws SandnodeErrorException {
        for (ThrowHandler f : throwHandlers) f.throwFunc(error);
    }

    public void handleError(RawMessage response) throws UnknownSandnodeErrorException, SandnodeErrorException {
        if (response.headers().type() == MessageTypes.ERR) {
            try {
                ErrorMessage errorMessage = new ErrorMessage(response);
                ServerErrorManager.instance().throwAll(errorMessage.error);
            } catch (ExpectedMessageException e) {
                throw new ImpossibleRuntimeException(e);
            }
        }
    }
}