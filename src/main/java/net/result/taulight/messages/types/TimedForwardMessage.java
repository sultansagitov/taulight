package net.result.taulight.messages.types;

import com.fasterxml.jackson.annotation.JsonProperty;
import net.result.sandnode.exceptions.DeserializationException;
import net.result.sandnode.exceptions.ExpectedMessageException;
import net.result.sandnode.messages.RawMessage;
import net.result.sandnode.messages.MSGPackMessage;

import java.time.ZonedDateTime;

import static net.result.taulight.messages.TauMessageTypes.FWD;

public class TimedForwardMessage extends MSGPackMessage<TimedForwardMessage.TimedForwardData> {
    public static class TimedForwardData extends ForwardMessage.ForwardData {
        @JsonProperty
        public ZonedDateTime zonedDateTime;

        public TimedForwardData() {}
        public TimedForwardData(String data, ZonedDateTime ztd) {
            super(data);
            zonedDateTime = ztd;
        }
    }

    public final String data;
    public final ZonedDateTime zdt;

    public TimedForwardMessage(ForwardMessage forwardMessage, ZonedDateTime zdt) {
        super(forwardMessage.getHeaders().setType(FWD), new TimedForwardData(forwardMessage.data, zdt));
        this.data = forwardMessage.data;
        this.zdt = zdt;
    }

    public TimedForwardMessage(RawMessage message) throws DeserializationException, ExpectedMessageException {
        super(message, TimedForwardData.class);
        ExpectedMessageException.check(message, FWD);
        data = object.content;
        zdt = object.zonedDateTime;
    }
}
