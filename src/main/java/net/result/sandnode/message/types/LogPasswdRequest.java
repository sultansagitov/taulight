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
        @JsonProperty
        public String nickname;
        @JsonProperty
        public String password;

        @SuppressWarnings("unused")
        public Data() {}

        public Data(String nickname, String password) {
            this.nickname = nickname;
            this.password = password;
        }
    }

    public LogPasswdRequest(@NotNull Headers headers, String nickname, String password) {
        super(headers.setType(MessageTypes.LOG_PASSWD), new Data(nickname, password));
    }

    public LogPasswdRequest(String nickname, String password) {
        this(new Headers(), nickname, password);
    }

    public LogPasswdRequest(RawMessage raw) throws DeserializationException, ExpectedMessageException {
        super(raw.expect(MessageTypes.LOG_PASSWD), Data.class);
    }

    public String getNickname() {
        return object.nickname;
    }

    public String getPassword() {
        return object.password;
    }
}
