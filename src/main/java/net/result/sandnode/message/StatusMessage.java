package net.result.sandnode.message;

import com.fasterxml.jackson.annotation.JsonProperty;
import net.result.sandnode.exception.DeserializationException;
import net.result.sandnode.message.util.Headers;
import org.jetbrains.annotations.NotNull;

public class StatusMessage extends MSGPackMessage<StatusMessage.CodeData> {
    public static class CodeData {
        @JsonProperty
        public int code;

        public CodeData() {}
        public CodeData(int code) {
            this.code = code;
        }
    }

    public StatusMessage(@NotNull Headers headers, int code) {
        super(headers, new CodeData(code));
    }

    public StatusMessage(IMessage response) throws DeserializationException {
        super(response, CodeData.class);
    }

    public int getCode() {
        return object.code;
    }
}
