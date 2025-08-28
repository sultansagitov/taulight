package net.result.sandnode.util;

import net.result.sandnode.serverclient.SandnodeClient;
import org.jetbrains.annotations.NotNull;

public record Member(String nickname, Address address) {
    public Member(@NotNull SandnodeClient client) {
        this(client.nickname, client.address);
    }

    @Override
    public @NotNull String toString() {
        return nickname + "@" + address;
    }
}
