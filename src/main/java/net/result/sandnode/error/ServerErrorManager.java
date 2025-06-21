package net.result.sandnode.error;

import net.result.sandnode.exception.ExpectedMessageException;
import net.result.sandnode.exception.ImpossibleRuntimeException;
import net.result.sandnode.exception.UnknownSandnodeErrorException;
import net.result.sandnode.exception.error.SandnodeErrorException;
import net.result.sandnode.message.RawMessage;
import net.result.sandnode.message.types.ErrorMessage;
import net.result.sandnode.message.util.MessageTypes;
import net.result.sandnode.util.Manager;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class ServerErrorManager extends Manager<SandnodeError> {
    private static final ServerErrorManager INSTANCE = new ServerErrorManager();
    private ServerErrorManager() {}
    public static ServerErrorManager instance() {
        return INSTANCE;
    }

    static {
        ServerErrorManager instance = instance();
        Arrays.stream(Errors.values()).forEach(instance::add);
    }

    @Override
    protected void handleOverflow(SandnodeError error) {
        list.removeIf(e -> e.code() == error.code());
    }

    public void handleError(@NotNull RawMessage response) throws UnknownSandnodeErrorException, SandnodeErrorException {
        if (response.headers().type() == MessageTypes.ERR) {
            try {
                ErrorMessage errorMessage = new ErrorMessage(response);
                throw errorMessage.error.exception();
            } catch (ExpectedMessageException e) {
                throw new ImpossibleRuntimeException(e);
            }
        }
    }
}