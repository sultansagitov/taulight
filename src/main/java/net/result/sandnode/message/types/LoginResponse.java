package net.result.sandnode.message.types;

import com.fasterxml.jackson.annotation.JsonProperty;
import net.result.sandnode.exception.DeserializationException;
import net.result.sandnode.message.IMessage;
import net.result.sandnode.message.MSGPackMessage;
import net.result.sandnode.message.util.Headers;
import net.result.sandnode.db.MemberEntity;
import net.result.sandnode.message.util.MessageTypes;
import org.jetbrains.annotations.NotNull;

public class LoginResponse extends MSGPackMessage<LoginResponse.LoginData> {
    public static class LoginData {
        @JsonProperty
        public String nickname;

        @SuppressWarnings("unused")
        public LoginData() {}

        public LoginData(String nickname) {
            this.nickname = nickname;
        }
    }

    public LoginResponse(@NotNull Headers headers, @NotNull MemberEntity member) {
        super(headers.setType(MessageTypes.LOGIN), new LoginData(member.nickname()));
    }

    public LoginResponse(MemberEntity member) {
        this(new Headers(), member);
    }

    public LoginResponse(IMessage message) throws DeserializationException {
        super(message, LoginData.class);
    }

    public String getNickname() {
        return object.nickname;
    }
}
