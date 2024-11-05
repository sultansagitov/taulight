package net.result.openhelo;

import net.result.sandnode.User;
import net.result.sandnode.config.UserConfig;
import net.result.sandnode.messages.RawMessage;
import net.result.sandnode.server.Session;
import net.result.sandnode.util.encryption.GlobalKeyStorage;
import org.jetbrains.annotations.NotNull;

public class HeloUser extends User {
    public HeloUser(@NotNull UserConfig userConfig) {
        this(new GlobalKeyStorage(), userConfig);
    }

    public HeloUser(@NotNull GlobalKeyStorage globalKeyStorage, @NotNull UserConfig userConfig) {
        super(globalKeyStorage, userConfig);
    }

    public HeloUser() {
        super();
    }

    @Override
    public void onUserMessage(
            @NotNull RawMessage request,
            @NotNull Session session
    ) {

    }
}
