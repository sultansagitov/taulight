package net.result.taulight.db;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.UUID;

public class ServerChatMessage {
    @JsonProperty
    private UUID id = null;
    @JsonProperty("server-ztd")
    private ZonedDateTime serverZtd = null;
    @JsonProperty
    private ChatMessage message;

    public UUID id() {
        return id;
    }

    public ZonedDateTime serverZtd() {
        return serverZtd;
    }

    public ChatMessage message() {
        return message;
    }

    public ServerChatMessage setServerZtd(ZonedDateTime serverZtd) {
        this.serverZtd = serverZtd;
        return this;
    }

    public ServerChatMessage setServerZtdNow() {
        return setServerZtd(ZonedDateTime.now(ZoneId.of("UTC")));
    }

    public ServerChatMessage setRandomID() {
        this.id = UUID.randomUUID();
        return this;
    }

    public ServerChatMessage setID(UUID id) {
        this.id = id;
        return this;
    }


    public ServerChatMessage setChatMessage(ChatMessage message) {
        this.message = message;
        return this;
    }

    @Override
    public String toString() {
        return "<ServerChatMessage id=%s, serverZtd=%s, message=%s>".formatted(id, serverZtd, message);
    }
}
