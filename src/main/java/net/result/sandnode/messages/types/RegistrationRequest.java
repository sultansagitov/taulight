package net.result.sandnode.messages.types;

import com.fasterxml.jackson.annotation.JsonProperty;
import net.result.sandnode.exceptions.DeserializationException;
import net.result.sandnode.exceptions.ExpectedMessageException;
import net.result.sandnode.messages.IMessage;
import net.result.sandnode.messages.MSGPackMessage;
import net.result.sandnode.messages.util.Headers;
import org.jetbrains.annotations.NotNull;

import static net.result.sandnode.messages.util.MessageTypes.REG;

public class RegistrationRequest extends MSGPackMessage<RegistrationRequest.MemberData> {
    public static class MemberData {
        @JsonProperty
        public String memberID;
        @JsonProperty
        public String password;

        public MemberData() {}
        public MemberData(@NotNull String memberID, @NotNull String password) {
            this.memberID = memberID;
            this.password = password;
        }
    }

    public RegistrationRequest(@NotNull IMessage message) throws ExpectedMessageException, DeserializationException {
        super(message, MemberData.class);
        ExpectedMessageException.check(message, REG);
    }

    public RegistrationRequest(@NotNull Headers headers, @NotNull String memberID, @NotNull String password) {
        super(headers.setType(REG), new MemberData(memberID, password));
    }

    public RegistrationRequest(@NotNull String memberID, @NotNull String password) {
        this(new Headers(), memberID, password);
    }

    public String getMemberID() {
        return object.memberID;
    }
    public String getPassword() {
        return object.password;
    }
}
