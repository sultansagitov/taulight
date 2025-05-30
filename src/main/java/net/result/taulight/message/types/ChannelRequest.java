package net.result.taulight.message.types;

import net.result.sandnode.exception.DeserializationException;
import net.result.sandnode.exception.ExpectedMessageException;
import net.result.sandnode.message.IMessage;
import net.result.sandnode.message.MSGPackMessage;
import net.result.sandnode.message.RawMessage;
import net.result.sandnode.message.util.Headers;
import net.result.taulight.dto.ChannelRequestDTO;
import net.result.taulight.message.TauMessageTypes;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class ChannelRequest extends MSGPackMessage<ChannelRequestDTO> {
    private ChannelRequest(ChannelRequestDTO data) {
        super(new Headers().setType(TauMessageTypes.CHANNEL), data);
    }

    public ChannelRequest(@NotNull RawMessage raw) throws ExpectedMessageException, DeserializationException {
        super(raw.expect(TauMessageTypes.CHANNEL), ChannelRequestDTO.class);
    }

    public static @NotNull ChannelRequest newChannel(String title) {
        ChannelRequestDTO data = new ChannelRequestDTO(ChannelRequestDTO.DataType.CREATE);
        data.title = title;

        return new ChannelRequest(data);
    }

    public static @NotNull ChannelRequest addMember(UUID chatID, String otherNickname, String expirationTime) {
        ChannelRequestDTO data = new ChannelRequestDTO(ChannelRequestDTO.DataType.INVITE);
        data.chatID = chatID;
        data.otherNickname = otherNickname;
        data.expirationTime = expirationTime;

        return new ChannelRequest(data);
    }

    public static @NotNull ChannelRequest channelCodes(UUID chatID) {
        ChannelRequestDTO data = new ChannelRequestDTO(ChannelRequestDTO.DataType.CH_CODES);
        data.chatID = chatID;

        return new ChannelRequest(data);
    }

    public static @NotNull ChannelRequest leave(UUID chatID) {
        ChannelRequestDTO data = new ChannelRequestDTO(ChannelRequestDTO.DataType.LEAVE);
        data.chatID = chatID;

        return new ChannelRequest(data);
    }

    public static @NotNull ChannelRequest myCodes() {
        return new ChannelRequest(new ChannelRequestDTO(ChannelRequestDTO.DataType.MY_CODES));
    }

    public static @NotNull IMessage setAvatar(UUID chatID) {
        ChannelRequestDTO data = new ChannelRequestDTO(ChannelRequestDTO.DataType.SET_AVATAR);
        data.chatID = chatID;

        return new ChannelRequest(data);
    }

    public static @NotNull IMessage getAvatar(UUID chatID) {
        ChannelRequestDTO data = new ChannelRequestDTO(ChannelRequestDTO.DataType.GET_AVATAR);
        data.chatID = chatID;

        return new ChannelRequest(data);
    }

    public ChannelRequestDTO dto() {
        return object;
    }
}
