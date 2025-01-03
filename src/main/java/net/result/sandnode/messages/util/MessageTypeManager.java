package net.result.sandnode.messages.util;

import net.result.sandnode.exceptions.NoSuchMessageTypeException;
import net.result.sandnode.util.Manager;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public class MessageTypeManager extends Manager<MessageType> {
    private static final MessageTypeManager INSTANCE = new MessageTypeManager();

    public static MessageTypeManager instance() {
        return INSTANCE;
    }

    private MessageTypeManager() {
        Arrays.stream(MessageTypes.values()).forEach(this::add);
    }

    @Override
    protected void handleOverflow(MessageType messageType) {

    }

    public static @NotNull MessageType getMessageType(byte type) throws NoSuchMessageTypeException {
        for (MessageType m : instance().list) {
            if (m.asByte() == type) {
                return m;
            }
        }
        throw new NoSuchMessageTypeException(type);
    }
}
