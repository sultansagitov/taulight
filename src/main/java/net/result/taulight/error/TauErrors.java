package net.result.taulight.error;

import net.result.sandnode.exception.SandnodeErrorException;
import net.result.sandnode.message.types.ErrorMessage;
import net.result.sandnode.error.SandnodeError;
import net.result.sandnode.error.ServerErrorManager;
import net.result.taulight.exception.ChatNotFoundException;
import net.result.taulight.exception.MessageNotForwardedException;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public enum TauErrors implements SandnodeError {
    CHAT_NOT_FOUND(4000, "Chat not found"),
    MESSAGE_NOT_FORWARDED(4001, "Message not forwarded");

    private final int code;
    private final String desc;

    TauErrors(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    @Override
    public int code() {
        return code;
    }

    @Override
    public String description() {
        return desc;
    }

    @Override
    @Contract(" -> new")
    public @NotNull ErrorMessage createMessage() {
        return new ErrorMessage(this);
    }

    public static void registerAll() {
        ServerErrorManager instance = ServerErrorManager.instance();
        Arrays.stream(TauErrors.values()).forEach(instance::add);
        instance.addThrowHandler(TauErrors::throwHandler);
    }

    private static void throwHandler(SandnodeError error) throws SandnodeErrorException {
        if (error instanceof TauErrors tau) {
            switch (tau) {
                case CHAT_NOT_FOUND -> throw new ChatNotFoundException();
                case MESSAGE_NOT_FORWARDED -> throw new MessageNotForwardedException();
            }
        }
    }
}
