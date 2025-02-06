package net.result.taulight.message.types;

import com.fasterxml.jackson.annotation.JsonProperty;
import net.result.sandnode.exception.DeserializationException;
import net.result.sandnode.message.MSGPackMessage;
import net.result.sandnode.message.RawMessage;
import net.result.sandnode.message.util.Headers;

import static net.result.taulight.message.TauMessageTypes.TAULIGHT;

public class TaulightRequest extends MSGPackMessage<TaulightRequest.TaulightRequestData> {
    public enum DataType {ADD, GET, REMOVE}

    public static class TaulightRequestData {
        @JsonProperty
        public DataType dataType;

        @JsonProperty
        public String chatID;
        public TaulightRequestData() {}

        public TaulightRequestData(DataType dataType) {
            this.dataType = dataType;
        }
        public static TaulightRequestData addGroup(String chatID) {
            TaulightRequestData result = new TaulightRequestData();
            result.dataType = DataType.ADD;
            result.chatID = chatID;
            return result;
        }

    }

    public TaulightRequest(Headers headers, TaulightRequestData taulightRequestData) {
        super(headers.setType(TAULIGHT), taulightRequestData);
    }

    public TaulightRequest(TaulightRequestData taulightRequestData) {
        this(new Headers(), taulightRequestData);
    }

    public TaulightRequest(DataType data) {
        this(new TaulightRequestData(data));
    }

    public TaulightRequest(RawMessage raw) throws DeserializationException {
        super(raw, TaulightRequestData.class);
    }

    public String getChatID() {
        return object.chatID;
    }

    public DataType getMessageType() {
        return object.dataType;
    }
}