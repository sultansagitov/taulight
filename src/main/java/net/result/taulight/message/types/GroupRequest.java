package net.result.taulight.message.types;

import net.result.sandnode.message.MSGPackMessage;
import net.result.sandnode.message.Message;
import net.result.sandnode.message.RawMessage;
import net.result.sandnode.message.util.Headers;
import net.result.taulight.dto.GroupRequestDTO;
import net.result.taulight.message.TauMessageTypes;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class GroupRequest extends MSGPackMessage<GroupRequestDTO> {
    private GroupRequest(GroupRequestDTO data) {
        super(new Headers().setType(TauMessageTypes.GROUP), data);
    }

    public GroupRequest(@NotNull RawMessage raw) {
        super(raw.expect(TauMessageTypes.GROUP), GroupRequestDTO.class);
    }

    public static @NotNull GroupRequest newGroup(String title) {
        GroupRequestDTO data = new GroupRequestDTO(GroupRequestDTO.DataType.CREATE);
        data.title = title;

        return new GroupRequest(data);
    }

    public static @NotNull GroupRequest addMember(UUID chatID, String otherNickname, String expirationTime) {
        GroupRequestDTO data = new GroupRequestDTO(GroupRequestDTO.DataType.INVITE);
        data.chatID = chatID;
        data.otherNickname = otherNickname;
        data.expirationTime = expirationTime;

        return new GroupRequest(data);
    }

    public static @NotNull GroupRequest leave(UUID chatID) {
        GroupRequestDTO data = new GroupRequestDTO(GroupRequestDTO.DataType.LEAVE);
        data.chatID = chatID;

        return new GroupRequest(data);
    }

    public static @NotNull Message setAvatar(UUID chatID) {
        GroupRequestDTO data = new GroupRequestDTO(GroupRequestDTO.DataType.SET_AVATAR);
        data.chatID = chatID;

        return new GroupRequest(data);
    }

    public static @NotNull Message getAvatar(UUID chatID) {
        GroupRequestDTO data = new GroupRequestDTO(GroupRequestDTO.DataType.GET_AVATAR);
        data.chatID = chatID;

        return new GroupRequest(data);
    }
}
