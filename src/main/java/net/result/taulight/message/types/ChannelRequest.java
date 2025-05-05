package net.result.taulight.message.types;

import net.result.sandnode.exception.DeserializationException;
import net.result.sandnode.exception.ExpectedMessageException;
import net.result.sandnode.exception.error.TooFewArgumentsException;
import net.result.sandnode.message.EmptyMessage;
import net.result.sandnode.message.IMessage;
import net.result.sandnode.message.RawMessage;
import net.result.sandnode.message.util.Headers;
import net.result.taulight.message.TauMessageTypes;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.UUID;

public class ChannelRequest extends EmptyMessage {
    public enum DataType {CREATE, INVITE, LEAVE, CH_CODES, MY_CODES, SET_AVATAR, GET_AVATAR}

    public DataType type;
    public String title;
    public UUID chatID;
    public String otherNickname;
    public Duration expirationTime;

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
                    this.type = DataType.CREATE;
                    this.title = headers()
                            .getOptionalValue("title")
                            .orElseThrow(TooFewArgumentsException::new);
                }
                case "add" -> {
                    this.type = DataType.INVITE;
                    this.chatID = headers()
                            .getOptionalValue("chat-id")
                            .map(UUID::fromString)
                            .orElseThrow(TooFewArgumentsException::new);

                    this.otherNickname = headers()
                            .getOptionalValue("other-nickname")
                            .orElseThrow(TooFewArgumentsException::new);

                    String s = headers().getOptionalValue("expires-in").orElseThrow(TooFewArgumentsException::new);
                    long seconds = Long.parseUnsignedLong(s);
                    this.expirationTime = Duration.ofSeconds(seconds);

                }
                case "leave" -> {
                    this.type = DataType.LEAVE;
                    this.chatID = headers()
                            .getOptionalValue("chat-id")
                            .map(UUID::fromString)
                            .orElseThrow(TooFewArgumentsException::new);
                }
                case "ch-codes" -> {
                    this.type = DataType.CH_CODES;
                    this.chatID = headers()
                            .getOptionalValue("chat-id")
                            .map(UUID::fromString)
                            .orElseThrow(TooFewArgumentsException::new);
                }
                case "my-codes" -> this.type = DataType.MY_CODES;
                case "set-avatar" -> {
                    this.type = DataType.SET_AVATAR;
                    this.chatID = headers()
                            .getOptionalValue("chat-id")
                            .map(UUID::fromString)
                            .orElseThrow(TooFewArgumentsException::new);
                }
                case "get-avatar" -> {
                    this.type = DataType.GET_AVATAR;
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

    public static @NotNull ChannelRequest addMember(UUID chatID, String otherNickname, Duration expirationTime) {
        Headers headers = new Headers()
                .setValue("type", "add")
                .setValue("chat-id", chatID.toString())
                .setValue("other-nickname", otherNickname)
                .setValue("expires-in", String.valueOf(expirationTime.toSeconds()));
        ChannelRequest request = new ChannelRequest(headers);
        request.chatID = chatID;
        request.otherNickname = otherNickname;

        return request;
    }

    public static @NotNull ChannelRequest channelCodes(UUID chatID) {
        Headers headers = new Headers()
                .setValue("type", "ch-codes")
                .setValue("chat-id", chatID.toString());
        ChannelRequest request = new ChannelRequest(headers);
        request.chatID = chatID;

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

    public static @NotNull ChannelRequest myCodes() {
        Headers headers = new Headers().setValue("type", "my-codes");
        ChannelRequest request = new ChannelRequest(headers);
        request.type = DataType.MY_CODES;
        return request;
    }

    public static @NotNull IMessage setAvatar(UUID chatID) {
        Headers headers = new Headers()
                .setValue("type", "set-avatar")
                .setValue("chat-id", chatID.toString());
        ChannelRequest request = new ChannelRequest(headers);
        request.type = DataType.SET_AVATAR;
        return request;
    }

    public static @NotNull IMessage getAvatar(UUID chatID) {
        Headers headers = new Headers()
                .setValue("type", "get-avatar")
                .setValue("chat-id", chatID.toString());
        ChannelRequest request = new ChannelRequest(headers);
        request.type = DataType.GET_AVATAR;
        return request;
    }
}
