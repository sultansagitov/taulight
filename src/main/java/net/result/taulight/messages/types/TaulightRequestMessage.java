package net.result.taulight.messages.types;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import net.result.sandnode.exceptions.DeserializationException;
import net.result.sandnode.messages.MSGPackMessage;
import net.result.sandnode.messages.RawMessage;
import net.result.sandnode.messages.util.Headers;
import net.result.taulight.messages.DataType;

import static net.result.taulight.messages.TauMessageTypes.TAULIGHT;

public class TaulightRequestMessage extends MSGPackMessage<TaulightRequestMessage.TaulightRequestData> {

    public static class TaulightRequestData {
        @JsonProperty
        public String message;
        @JsonProperty
        private DataType dataType;

        public TaulightRequestData() {}
        public TaulightRequestData(DataType dataType) {
            this.dataType = dataType;
        }

        public TaulightRequestData(DataType dataType, String message) {
            this(dataType);
            this.message = message;
        }
    }

    public TaulightRequestMessage(Headers headers, TaulightRequestData taulightRequestData) {
        super(headers.setType(TAULIGHT), taulightRequestData);
    }

    public TaulightRequestMessage(TaulightRequestData taulightRequestData) {
        this(new Headers(), taulightRequestData);
    }

    public TaulightRequestMessage(DataType data) {
        this(new TaulightRequestData(data));
    }

    public TaulightRequestMessage(RawMessage raw) throws DeserializationException {
        super(raw, TaulightRequestData.class);
    }

    public DataType getMessageType() {
        return object.dataType;
    }
}