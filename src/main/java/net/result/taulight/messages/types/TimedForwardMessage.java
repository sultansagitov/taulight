package net.result.taulight.messages.types;

import net.result.sandnode.messages.RawMessage;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public class TimedForwardMessage extends ForwardMessage {
    private final ZonedDateTime zdt;

    public TimedForwardMessage(ForwardMessage forwardMessage, ZonedDateTime zdt) {
        super(forwardMessage.getHeaders().copy(), forwardMessage.data);
        this.zdt = zdt;
        getContent().put("dt", zdt.toEpochSecond());
    }

    public TimedForwardMessage(RawMessage message) {
        super(message);
        long dt = getContent().getNumber("dt").longValue();
        Instant instant = Instant.ofEpochSecond(dt);
        zdt = ZonedDateTime.ofInstant(instant, ZoneId.of("UTC"));
    }
}
