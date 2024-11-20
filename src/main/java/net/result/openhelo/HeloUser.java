package net.result.openhelo;

import net.result.sandnode.User;
import net.result.sandnode.config.IUserConfig;
import net.result.sandnode.messages.IMessage;
import net.result.sandnode.server.Session;
import net.result.sandnode.util.encryption.GlobalKeyStorage;
import org.jetbrains.annotations.NotNull;

public class HeloUser extends User {

    public HeloUser(@NotNull IUserConfig userConfig) {
        this(new GlobalKeyStorage(), userConfig);
    }

    public HeloUser(@NotNull GlobalKeyStorage globalKeyStorage, @NotNull IUserConfig userConfig) {
        super(globalKeyStorage, userConfig);
    }

    @Override
    public void onUserMessage(
            @NotNull IMessage request,
            @NotNull Session session
    ) {
    }
}
