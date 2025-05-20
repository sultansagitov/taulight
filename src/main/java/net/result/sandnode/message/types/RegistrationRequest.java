package net.result.sandnode.message.types;

import com.fasterxml.jackson.annotation.JsonProperty;
import net.result.sandnode.exception.DeserializationException;
import net.result.sandnode.exception.ExpectedMessageException;
import net.result.sandnode.message.MSGPackMessage;
import net.result.sandnode.message.RawMessage;
import net.result.sandnode.message.util.Headers;
import net.result.sandnode.message.util.MessageTypes;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class RegistrationRequest extends MSGPackMessage<RegistrationRequest.MemberData> {
    protected static class MemberData {
        @JsonProperty
        public String nickname;
        @JsonProperty
        public String password;
        @JsonProperty
        public String device;
        @JsonProperty
        public Map<String, String> keyStorage;

        @SuppressWarnings("unused")
        public MemberData() {}

        public MemberData(String nickname, String password, String device, Map<String, String> keyStorage) {
            this.nickname = nickname;
            this.password = password;
            this.device = device;
            this.keyStorage = keyStorage;
        }
    }

    public RegistrationRequest(@NotNull RawMessage message) throws ExpectedMessageException, DeserializationException {
        super(message.expect(MessageTypes.REG), MemberData.class);
    }

    public RegistrationRequest(
            @NotNull String nickname,
            @NotNull String password,
            @NotNull String device,
            @NotNull Map<String, String> keyStorage
    ) {
        super(new Headers().setType(MessageTypes.REG), new MemberData(nickname, password, device, keyStorage));
    }

    public String getNickname() {
        return object.nickname;
    }

    public String getPassword() {
        return object.password;
    }

    public String getDevice() {
        return object.device;
    }

    public Map<String, String> getKeyStorage() {
        return object.keyStorage;
    }
}
