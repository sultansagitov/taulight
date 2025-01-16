package net.result.taulight.messages.types;

import com.fasterxml.jackson.annotation.JsonProperty;
import net.result.sandnode.client.ClientMember;
import net.result.sandnode.exceptions.DeserializationException;
import net.result.sandnode.exceptions.ExpectedMessageException;
import net.result.sandnode.messages.RawMessage;
import net.result.sandnode.messages.MSGPackMessage;
import net.result.sandnode.util.db.IMember;

import java.time.ZonedDateTime;

import static net.result.taulight.messages.TauMessageTypes.FWD;

public class TimedForwardMessage extends MSGPackMessage<TimedForwardMessage.TimedForwardData> {
    public static class TimedForwardData extends ForwardMessage.ForwardData {
        @JsonProperty
        public ZonedDateTime zonedDateTime;
        @JsonProperty
        public ClientMember clientMember;

        public TimedForwardData() {}
        public TimedForwardData(String chatID, String data, ZonedDateTime ztd, ClientMember clientMember) {
            super(chatID, data);
            zonedDateTime = ztd;
            this.clientMember = clientMember;
        }
    }

    public TimedForwardMessage(ForwardMessage forwardMessage, ZonedDateTime zdt, IMember member) {
        super(forwardMessage.getHeaders(), new TimedForwardData(
                forwardMessage.getChatID(),
                forwardMessage.getData(),
                zdt,
                ClientMember.of(member)
        ));
    }

    public TimedForwardMessage(RawMessage message) throws DeserializationException, ExpectedMessageException {
        super(message, TimedForwardData.class);
        ExpectedMessageException.check(message, FWD);
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
