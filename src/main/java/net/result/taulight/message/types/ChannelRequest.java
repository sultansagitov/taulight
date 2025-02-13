package net.result.taulight.message.types;

import net.result.sandnode.exception.DeserializationException;
import net.result.sandnode.message.MSGPackMessage;
import net.result.sandnode.message.RawMessage;
import net.result.sandnode.message.util.Headers;
import net.result.sandnode.serverclient.ClientMember;
import net.result.taulight.message.TauMessageTypes;

public class ChannelRequest extends MSGPackMessage<ChannelRequest.Data> {
    public enum DataType {NEW, LEAVE, ADD}

    public static class Data {
        public DataType type;
        public String title;
        public ClientMember member;
        public String chatID;

        public Data() {}

        private Data(DataType type) {
            this.type = type;
        }

        private Data(DataType type, String title) {
            this.type = type;
            this.title = title;
        }
    }

    private ChannelRequest(Data object) {
        super(new Headers().setType(TauMessageTypes.CHANNEL), object);
    }

    public ChannelRequest(RawMessage raw) throws DeserializationException {
        super(raw, Data.class);
    }


    public static ChannelRequest newChannel(String title) {
        return new ChannelRequest(new Data(DataType.NEW, title));
    }

    public static ChannelRequest leave(String chatID) {
        Data data = new Data(DataType.LEAVE);
        data.chatID = chatID;
        return new ChannelRequest(data);
    }

    public static ChannelRequest addMember(String chatID, ClientMember member) {
        Data data = new Data(DataType.ADD);
        data.chatID = chatID;
        data.member = member;
        return new ChannelRequest(data);
    }
}
