package net.result.openhelo;

import net.result.sandnode.messages.RawMessage;
import net.result.sandnode.server.Session;
import net.result.sandnode.User;
import net.result.sandnode.util.encryption.GlobalKeyStorage;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class HeloUser extends User {
    public HeloUser(@NotNull GlobalKeyStorage globalKeyStorage) {
        super(globalKeyStorage);
    }

    public HeloUser() {
        super();
    }

    @Override
    public void onUserMessage(
            @NotNull RawMessage request,
            @NotNull List<Session> sessionList,
            @NotNull Session session
    ) {

    }
}
