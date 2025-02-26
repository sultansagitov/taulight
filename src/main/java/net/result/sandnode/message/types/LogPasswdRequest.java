package net.result.sandnode.message.types;

import com.fasterxml.jackson.annotation.JsonProperty;
import net.result.sandnode.exception.DeserializationException;
import net.result.sandnode.exception.ExpectedMessageException;
import net.result.sandnode.message.MSGPackMessage;
import net.result.sandnode.message.RawMessage;
import net.result.sandnode.message.util.Headers;
import net.result.sandnode.message.util.MessageTypes;
import org.jetbrains.annotations.NotNull;

public class LogPasswdRequest extends MSGPackMessage<LogPasswdRequest.Data> {
    protected static class Data {
        @JsonProperty("member-id")
        public String memberID;
        @JsonProperty
        public String password;

        @SuppressWarnings("unused")
        public Data() {}

        public Data(String memberID, String password) {
            this.memberID = memberID;
            this.password = password;
        }
    }

    public LogPasswdRequest(@NotNull Headers headers, String memberID, String password) {
        super(headers.setType(MessageTypes.LOG_PASSWD), new Data(memberID, password));
    }

    public LogPasswdRequest(String memberID, String password) {
        this(new Headers(), memberID, password);
    }

    public LogPasswdRequest(RawMessage raw) throws DeserializationException, ExpectedMessageException {
        super(raw.expect(MessageTypes.LOG_PASSWD), Data.class);
    }

    public String getMemberID() {
        return object.memberID;
    }

    public String getPassword() {
        return object.password;
    }
}
