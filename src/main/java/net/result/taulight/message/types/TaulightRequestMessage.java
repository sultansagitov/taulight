package net.result.taulight.message.types;

import com.fasterxml.jackson.annotation.JsonProperty;
import net.result.sandnode.exception.DeserializationException;
import net.result.sandnode.message.MSGPackMessage;
import net.result.sandnode.message.RawMessage;
import net.result.sandnode.message.util.Headers;
import net.result.taulight.message.DataType;

import static net.result.taulight.message.TauMessageTypes.TAULIGHT;

public class TaulightRequestMessage extends MSGPackMessage<TaulightRequestMessage.TaulightRequestData> {

    public static class TaulightRequestData {
        @JsonProperty
        public String message;
        @JsonProperty
        public DataType dataType;
        @JsonProperty
        public String chatID;

        public TaulightRequestData() {}
        public TaulightRequestData(DataType dataType) {
            this.dataType = dataType;
        }

        public static TaulightRequestData write(String chatID, String message) {
            TaulightRequestData result = new TaulightRequestData();
            result.dataType = DataType.WRITE;
            result.chatID = chatID;
            result.message = message;
            return result;
        }

        public static TaulightRequestData addGroup(String chatID) {
            TaulightRequestData result = new TaulightRequestData();
            result.dataType = DataType.ADD;
            result.chatID = chatID;
            return result;
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

    public String getChatID() {
        return object.chatID;
    }

    public DataType getMessageType() {
        return object.dataType;
    }
}