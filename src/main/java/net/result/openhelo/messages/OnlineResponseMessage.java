package net.result.openhelo.messages;

import net.result.openhelo.HeloType;

import java.util.List;

import static net.result.openhelo.HeloType.ONLINE_RESPONSE;

public class OnlineResponseMessage extends HeloMessage {
    public final List<String> users;

    public OnlineResponseMessage(List<String> users) {
        this.users = users;
    }

    public OnlineResponseMessage(String[] users) {
        this.users = List.of(users);
    }

    @Override
    public HeloType getType() {
        return ONLINE_RESPONSE;
    }

    @Override
    public byte[] toByteArray() {
        return String.join(",", users).getBytes();
    }
}
