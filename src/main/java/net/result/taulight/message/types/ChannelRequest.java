package net.result.taulight.message.types;

import net.result.sandnode.exception.DeserializationException;
import net.result.sandnode.exception.ExpectedMessageException;
import net.result.sandnode.exception.error.TooFewArgumentsException;
import net.result.sandnode.message.EmptyMessage;
import net.result.sandnode.message.RawMessage;
import net.result.sandnode.message.util.Headers;
import net.result.taulight.message.TauMessageTypes;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class ChannelRequest extends EmptyMessage {
    public enum DataType {NEW, ADD, LEAVE}

    public DataType type;
    public String title;
    public UUID chatID;
    public String otherNickname;

    private ChannelRequest(@NotNull Headers headers) {
        super(headers.setType(TauMessageTypes.CHANNEL));
    }

    public ChannelRequest(@NotNull RawMessage raw)
            throws ExpectedMessageException, TooFewArgumentsException, DeserializationException {
        super(raw.expect(TauMessageTypes.CHANNEL).headers());
        String type = headers().getOptionalValue("type").orElseThrow(TooFewArgumentsException::new);
        try {
            switch (type) {
                case "new" -> {
                    this.type = DataType.NEW;
                    this.title = headers()
                            .getOptionalValue("title")
                            .orElseThrow(TooFewArgumentsException::new);
                }
                case "add" -> {
                    this.type = DataType.ADD;
                    this.chatID = headers()
                            .getOptionalValue("chat-id")
                            .map(UUID::fromString)
                            .orElseThrow(TooFewArgumentsException::new);

                    this.otherNickname = headers()
                            .getOptionalValue("other-nickname")
                            .orElseThrow(TooFewArgumentsException::new);

                }
                case "leave" -> {
                    this.type = DataType.LEAVE;
                    this.chatID = headers()
                            .getOptionalValue("chat-id")
                            .map(UUID::fromString)
                            .orElseThrow(TooFewArgumentsException::new);
                }
                default -> throw new DeserializationException("Incorrect type field - \"%s\"".formatted(type));
            }
        } catch (IllegalArgumentException e) {
            throw new DeserializationException(e);
        }
    }


    public static @NotNull ChannelRequest newChannel(String title) {
        Headers headers = new Headers()
                .setValue("type", "new")
                .setValue("title", title);
        ChannelRequest request = new ChannelRequest(headers);
        request.title = title;

        return request;
    }

    public static @NotNull ChannelRequest addMember(UUID chatID, String otherNickname) {
        Headers headers = new Headers()
                .setValue("type", "add")
                .setValue("chat-id", chatID.toString())
                .setValue("other-nickname", otherNickname);
        ChannelRequest request = new ChannelRequest(headers);
        request.chatID = chatID;
        request.otherNickname = otherNickname;

        return request;
    }

    public static @NotNull ChannelRequest leave(UUID chatID) {
        Headers headers = new Headers()
                .setValue("type", "leave")
                .setValue("chat-id", chatID.toString());
        ChannelRequest request = new ChannelRequest(headers);
        request.chatID = chatID;

        return request;
    }
}
