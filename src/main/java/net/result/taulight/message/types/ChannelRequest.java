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
import java.util.Arrays;
import java.util.UUID;

public class ChannelRequest extends EmptyMessage {
    public enum DataType {
        CREATE("new"),
        INVITE("add"),
        LEAVE("leave"),
        CH_CODES("ch-codes"),
        MY_CODES("my-codes"),
        SET_AVATAR("set-avatar"),
        GET_AVATAR("get-avatar");

        private final String value;

        DataType(String wireValue) {
            this.value = wireValue;
        }

        public static DataType fromValue(String value) {
            return Arrays.stream(values())
                    .filter(type -> type.value.equals(value))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Unknown type: " + value));
        }
    }


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
        String typeString = headers().getOptionalValue("type").orElseThrow(TooFewArgumentsException::new);

        this.type = DataType.fromValue(typeString);

        try {
            switch (type) {
                case CREATE -> this.title = headers()
                        .getOptionalValue("title")
                        .orElseThrow(TooFewArgumentsException::new);
                case INVITE -> {
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
                case LEAVE, CH_CODES, SET_AVATAR, GET_AVATAR -> this.chatID = headers()
                        .getOptionalValue("chat-id")
                        .map(UUID::fromString)
                        .orElseThrow(TooFewArgumentsException::new);
                case MY_CODES -> {}
                default -> throw new DeserializationException("Incorrect type field - \"%s\"".formatted(typeString));
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
