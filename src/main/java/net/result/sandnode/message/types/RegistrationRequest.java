package net.result.sandnode.message.types;

import com.fasterxml.jackson.annotation.JsonProperty;
import net.result.sandnode.exception.DeserializationException;
import net.result.sandnode.exception.ExpectedMessageException;
import net.result.sandnode.message.MSGPackMessage;
import net.result.sandnode.message.RawMessage;
import net.result.sandnode.message.util.Headers;
import net.result.sandnode.message.util.MessageTypes;
import org.jetbrains.annotations.NotNull;

public class RegistrationRequest extends MSGPackMessage<RegistrationRequest.MemberData> {
    public static class MemberData {
        @JsonProperty
        public String nickname;
        @JsonProperty
        public String password;

        @SuppressWarnings("unused")
        public MemberData() {}

        public MemberData(@NotNull String nickname, @NotNull String password) {
            this.nickname = nickname;
            this.password = password;
        }
    }

    public RegistrationRequest(@NotNull RawMessage message) throws ExpectedMessageException, DeserializationException {
        super(message.expect(MessageTypes.REG), MemberData.class);
    }

    public RegistrationRequest(@NotNull Headers headers, @NotNull String nickname, @NotNull String password) {
        super(headers.setType(MessageTypes.REG), new MemberData(nickname, password));
    }

    public RegistrationRequest(@NotNull String nickname, @NotNull String password) {
        this(new Headers(), nickname, password);
    }

    public String getNickname() {
        return object.nickname;
    }

    public String getPassword() {
        return object.password;
    }
}
