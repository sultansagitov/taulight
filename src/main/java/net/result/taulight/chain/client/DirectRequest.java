package net.result.taulight.chain.client;

import com.fasterxml.jackson.annotation.JsonProperty;
import net.result.sandnode.exception.DeserializationException;
import net.result.sandnode.exception.ExpectedMessageException;
import net.result.sandnode.message.MSGPackMessage;
import net.result.sandnode.message.RawMessage;
import net.result.sandnode.message.util.Headers;
import net.result.taulight.message.TauMessageTypes;

public class DirectRequest extends MSGPackMessage<DirectRequest.Data> {
    protected static class Data {
        @JsonProperty
        public String memberID;

        @SuppressWarnings("unused")
        public Data() {}
        public Data(String memberID) {
            this.memberID = memberID;
        }
    }

    public DirectRequest(Headers headers, String memberID) {
        super(headers.setType(TauMessageTypes.DIRECT), new Data(memberID));
    }

    public DirectRequest(String memberID) {
        this(new Headers(), memberID);
    }

    public DirectRequest(RawMessage raw) throws ExpectedMessageException, DeserializationException {
        super(raw.expect(TauMessageTypes.DIRECT), Data.class);
    }

    public String getMemberID() {
        return object.memberID;
    }
}
