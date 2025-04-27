package net.result.taulight.message.types;

import com.fasterxml.jackson.annotation.JsonProperty;
import net.result.sandnode.exception.DeserializationException;
import net.result.sandnode.exception.ExpectedMessageException;
import net.result.sandnode.message.MSGPackMessage;
import net.result.sandnode.message.RawMessage;
import net.result.sandnode.message.util.Headers;
import net.result.taulight.message.TauMessageTypes;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class RoleRequest extends MSGPackMessage<RoleRequest.Data> {
    public enum DataType {GET, CREATE, ADD}

    protected static class Data {
        @JsonProperty("type")
        private DataType dataType = null;
        @JsonProperty("chat-id")
        private UUID chatID = null;
        @JsonProperty("role")
        private String roleName = null;
        @JsonProperty("nickname")
        private String nickname = null;

        @SuppressWarnings("unused")
        public Data() {}

        public Data(DataType dataType, UUID chatID, String roleName, String nickname) {
            this.dataType = dataType;
            this.chatID = chatID;
            this.roleName = roleName;
            this.nickname = nickname;
        }
    }

    private RoleRequest(Data data) {
        super(new Headers().setType(TauMessageTypes.ROLES), data);
    }

    public static @NotNull RoleRequest getRoles(UUID chatID) {
        return new RoleRequest(new Data(DataType.GET, chatID, null, null));
    }

    public static @NotNull RoleRequest addRole(UUID chatID, String roleName) {
        return new RoleRequest(new Data(DataType.CREATE, chatID, roleName, null));
    }

    public static @NotNull RoleRequest assignRole(UUID chatID, String roleName, String nickname) {
        return new RoleRequest(new Data(DataType.ADD, chatID, roleName, nickname));
    }

    public RoleRequest(RawMessage raw) throws DeserializationException, ExpectedMessageException {
        super(raw.expect(TauMessageTypes.ROLES), Data.class);
    }

    public DataType getDataType() {
        return object.dataType;
    }

    public UUID getChatID() {
        return object.chatID;
    }

    public String getRoleName() {
        return object.roleName;
    }

    public String getNickname() {
        return object.nickname;
    }
}
