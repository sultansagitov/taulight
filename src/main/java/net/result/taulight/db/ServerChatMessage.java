package net.result.taulight.db;

import com.fasterxml.jackson.annotation.JsonProperty;
import net.result.sandnode.db.SandnodeObject;

import java.time.ZoneId;
import java.time.ZonedDateTime;

public class ServerChatMessage extends SandnodeObject {
    @JsonProperty("server-ztd")
    private ZonedDateTime serverZtd = null;
    @JsonProperty
    private ChatMessage message;

    public ZonedDateTime serverZtd() {
        return serverZtd;
    }

    public ChatMessage message() {
        return message;
    }

    public void setServerZtd(ZonedDateTime serverZtd) {
        this.serverZtd = serverZtd;
    }

    public void setServerZtdNow() {
        setServerZtd(ZonedDateTime.now(ZoneId.of("UTC")));
    }

    public void setChatMessage(ChatMessage message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "<ServerChatMessage id=%s, serverZtd=%s, message=%s>".formatted(id(), serverZtd, message);
    }
}
