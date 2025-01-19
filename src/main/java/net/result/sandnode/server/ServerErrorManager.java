package net.result.sandnode.server;

import net.result.sandnode.util.Manager;

public class ServerErrorManager extends Manager<ServerErrorInterface> {
    private static final ServerErrorManager INSTANCE = new ServerErrorManager();

    private ServerErrorManager() {}

    public static ServerErrorManager instance() {
        return INSTANCE;
    }

    static {
        for (ServerError value : ServerError.values()) {
            instance().add(value);
        }
    }

    @Override
    protected void handleOverflow(ServerErrorInterface error) {
        list.removeIf(e -> e.getCode() == error.getCode());
    }
}