package net.result.taulight.message.types;

import com.fasterxml.jackson.annotation.JsonProperty;
import net.result.sandnode.serverclient.ClientMember;
import net.result.sandnode.exception.DeserializationException;
import net.result.sandnode.exception.ExpectedMessageException;
import net.result.sandnode.message.RawMessage;
import net.result.sandnode.message.MSGPackMessage;
import net.result.sandnode.db.Member;

import java.time.ZonedDateTime;

import static net.result.taulight.message.TauMessageTypes.FWD;

public class TimedForwardMessage extends MSGPackMessage<TimedForwardMessage.TimedForwardData> {
    public static class TimedForwardData extends ForwardMessage.ForwardData {
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

    public TimedForwardMessage(ForwardMessage forwardMessage, ZonedDateTime zdt, Member member) {
        super(forwardMessage.getHeaders(), new TimedForwardData(
                forwardMessage.getChatID(),
                forwardMessage.getData(),
                zdt,
                ClientMember.of(member)
        ));
    }

    public TimedForwardMessage(RawMessage message) throws DeserializationException, ExpectedMessageException {
        super(message.expect(FWD), TimedForwardData.class);
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
