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
        @JsonProperty("member-id")
        public String memberID;
        @JsonProperty
        public String password;

        @SuppressWarnings("unused")
        public MemberData() {}
        public MemberData(@NotNull String memberID, @NotNull String password) {
            this.memberID = memberID;
            this.password = password;
        }
    }

    public RegistrationRequest(@NotNull RawMessage message) throws ExpectedMessageException, DeserializationException {
        super(message.expect(MessageTypes.REG), MemberData.class);
    }

    public RegistrationRequest(@NotNull Headers headers, @NotNull String memberID, @NotNull String password) {
        super(headers.setType(MessageTypes.REG), new MemberData(memberID, password));
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
