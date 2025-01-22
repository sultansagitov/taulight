package net.result.sandnode.message.types;

import com.fasterxml.jackson.annotation.JsonProperty;
import net.result.sandnode.exception.DeserializationException;
import net.result.sandnode.message.IMessage;
import net.result.sandnode.message.MSGPackMessage;
import net.result.sandnode.message.util.Headers;
import net.result.sandnode.db.IMember;
import org.jetbrains.annotations.NotNull;

import static net.result.sandnode.message.util.MessageTypes.LOGIN;

public class LoginResponse extends MSGPackMessage<LoginResponse.LoginData> {
    public static class LoginData {
        @JsonProperty
        public String memberID;

        public LoginData() {}
        public LoginData(String memberID) {
            this.memberID = memberID;
        }
    }

    public LoginResponse(@NotNull Headers headers, @NotNull IMember member) {
        super(headers.setType(LOGIN), new LoginData(member.getID()));
    }

    public LoginResponse(IMember member) {
        this(new Headers(), member);
    }

    public LoginResponse(IMessage message) throws DeserializationException {
        super(message, LoginData.class);
    }

    public String getMemberID() {
        return object.memberID;
    }
}
