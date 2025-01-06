package net.result.taulight.messages.types;

import net.result.sandnode.exceptions.DeserializationException;
import net.result.sandnode.exceptions.ExpectedMessageException;
import net.result.sandnode.messages.IMessage;
import net.result.sandnode.messages.MSGPackMessage;
import net.result.sandnode.messages.util.Headers;
import net.result.taulight.messages.types.ForwardMessage.ForwardData;

import static net.result.taulight.messages.TauMessageTypes.ECHO;

public class EchoMessage extends MSGPackMessage<ForwardData> {
    public EchoMessage(Headers headers, String data) {
        super(headers.setType(ECHO), new ForwardData(data));
    }

    public EchoMessage(String data) {
        this(new Headers(), data);
    }

    public EchoMessage(IMessage request) throws DeserializationException, ExpectedMessageException {
        super(request, ForwardData.class);
        ExpectedMessageException.check(request, ECHO);
    }

    public String getData() {
        return object.content;
    }
}
