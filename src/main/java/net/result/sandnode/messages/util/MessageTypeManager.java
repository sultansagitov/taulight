package net.result.sandnode.messages.util;

import net.result.sandnode.exceptions.NoSuchMessageTypeException;
import net.result.sandnode.util.Manager;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public class MessageTypeManager extends Manager<IMessageType> {
    private static final MessageTypeManager INSTANCE = new MessageTypeManager();

    public static MessageTypeManager instance() {
        return INSTANCE;
    }

    private MessageTypeManager() {
        Arrays.stream(MessageType.values()).forEach(this::add);
    }

    @Override
    protected void handleOverflow(IMessageType messageType) {

    }

    public static @NotNull IMessageType getMessageType(byte type) throws NoSuchMessageTypeException {
        for (IMessageType m : instance().list) {
            if (m.asByte() == type) {
                return m;
            }
        }
        throw new NoSuchMessageTypeException(type);
    }
}
