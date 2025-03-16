package net.result.taulight.message.types;

import net.result.sandnode.exception.DeserializationException;
import net.result.sandnode.exception.ExpectedMessageException;
import net.result.sandnode.message.MSGPackMessage;
import net.result.sandnode.message.RawMessage;
import net.result.sandnode.message.util.Headers;
import net.result.taulight.db.InviteToken;
import net.result.taulight.db.TauChannel;
import net.result.taulight.message.TauMessageTypes;
import org.jetbrains.annotations.NotNull;

import java.time.ZonedDateTime;

public class TauCodeResponse extends MSGPackMessage<TauCodeResponse.Data> {
    public enum DataType {INVITE}

    public static class Data {
        public DataType type;
        public String title;
        public ZonedDateTime expiresData;

        @SuppressWarnings("unused")
        public Data() {}
        public Data(InviteToken it, TauChannel channel) {
            type = DataType.INVITE;
            title = channel.title();
            expiresData = it.getExpiresData();
        }
    }

    public TauCodeResponse(Data data) {
        this(new Headers(), data);
    }

    public TauCodeResponse(@NotNull Headers headers, Data data) {
        super(headers.setType(TauMessageTypes.CODE), data);
    }

    public TauCodeResponse(@NotNull RawMessage raw) throws DeserializationException, ExpectedMessageException {
        super(raw.expect(TauMessageTypes.CODE), Data.class);
    }
}
