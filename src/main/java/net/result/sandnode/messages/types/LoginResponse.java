package net.result.sandnode.messages.types;

import com.fasterxml.jackson.annotation.JsonProperty;
import net.result.sandnode.exceptions.DeserializationException;
import net.result.sandnode.messages.IMessage;
import net.result.sandnode.messages.MSGPackMessage;
import net.result.sandnode.messages.util.Headers;
import net.result.sandnode.util.db.IMember;
import org.jetbrains.annotations.NotNull;

import static net.result.sandnode.messages.util.MessageTypes.LOGIN;

public class LoginResponse extends MSGPackMessage<LoginResponse.LoginData> {
    public static class LoginData {
        @JsonProperty
        String memberID;

        public LoginData() {}
        public LoginData(String memberID) {
            this.memberID = memberID;
        }
    }

    private final String memberID;

    public LoginResponse(@NotNull Headers headers, @NotNull IMember member) {
        super(headers.setType(LOGIN), new LoginData(member.getID()));
        memberID = object.memberID;
    }

    public LoginResponse(IMember member) {
        this(new Headers(), member);
    }

    public LoginResponse(IMessage message) throws DeserializationException {
        super(message, LoginData.class);
        memberID = object.memberID;
    }

    public String getMemberID() {
        return memberID;
    }
}
