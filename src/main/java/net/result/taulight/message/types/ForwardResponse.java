package net.result.taulight.message.types;

import com.fasterxml.jackson.annotation.JsonProperty;
import net.result.sandnode.serverclient.ClientMember;
import net.result.sandnode.exception.DeserializationException;
import net.result.sandnode.exception.ExpectedMessageException;
import net.result.sandnode.message.RawMessage;
import net.result.sandnode.message.MSGPackMessage;
import net.result.sandnode.db.Member;
import net.result.taulight.message.TauMessageTypes;

import java.time.ZonedDateTime;

public class ForwardResponse extends MSGPackMessage<ForwardResponse.TimedForwardData> {
    public static class TimedForwardData extends ForwardRequest.ForwardData {
        @JsonProperty
        public ZonedDateTime zonedDateTime;
        @JsonProperty
        public ClientMember clientMember;

        @SuppressWarnings("unused")
        public TimedForwardData() {}
        public TimedForwardData(String chatID, String data, ZonedDateTime ztd, ClientMember clientMember) {
            super(chatID, data);
            zonedDateTime = ztd;
            this.clientMember = clientMember;
        }
    }

    public ForwardResponse(ForwardRequest forwardMessage, ZonedDateTime zdt, Member member) {
        super(forwardMessage.getHeaders().setType(TauMessageTypes.FWD), new TimedForwardData(
                forwardMessage.getChatID(),
                forwardMessage.getData(),
                zdt,
                ClientMember.of(member)
        ));
    }

    public ForwardResponse(RawMessage message) throws DeserializationException, ExpectedMessageException {
        super(message.expect(TauMessageTypes.FWD), TimedForwardData.class);
    }

    public String getData() {
        return object.content;
    }

    public String getChatID() {
        return object.chatID;
    }

    public ZonedDateTime getZonedDateTime() {
        return object.zonedDateTime;
    }

    public ClientMember getMember() {
        return object.clientMember;
    }
}
