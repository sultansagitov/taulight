package net.result.sandnode.error;

import net.result.sandnode.exception.SandnodeErrorException;
import net.result.sandnode.util.Manager;

import java.util.*;

public class ServerErrorManager extends Manager<SandnodeError> {
    public interface ThrowHandler {
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
}