package net.result.sandnode.util;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import net.result.sandnode.serverclient.SandnodeClient;
import org.jetbrains.annotations.NotNull;

public record Member(String nickname, Address address) {
    public Member(@NotNull SandnodeClient client) {
        this(client.nickname, client.address);
    }

    @Override
    @JsonValue
    public @NotNull String toString() {
        return nickname + "@" + address;
    }

    @JsonCreator
    public static Member fromString(String raw) {
        int sep = raw.indexOf('@');
        if (sep < 0) {
            throw new IllegalArgumentException("Invalid Member format: " + raw);
        }
        String nickname = raw.substring(0, sep);
        Address addr = Address.getFromString(raw.substring(sep + 1));
        return new Member(nickname, addr);
    }
}
