package net.result.sandnode.error;

import net.result.sandnode.util.Manager;

public class ServerErrorManager extends Manager<SandnodeError> {
    private static final ServerErrorManager INSTANCE = new ServerErrorManager();

    private ServerErrorManager() {}

    public static ServerErrorManager instance() {
        return INSTANCE;
    }

    static {
        for (Errors value : Errors.values()) {
            instance().add(value);
        }
    }

    @Override
    protected void handleOverflow(SandnodeError error) {
        list.removeIf(e -> e.getCode() == error.getCode());
    }
}