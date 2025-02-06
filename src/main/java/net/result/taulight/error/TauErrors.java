package net.result.taulight.error;

import net.result.sandnode.message.types.ErrorMessage;
import net.result.sandnode.error.SandnodeError;
import net.result.sandnode.error.ServerErrorManager;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

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
    public int getCode() {
        return code;
    }

    @Override
    public String getDescription() {
        return desc;
    }

    @Override
    @Contract(" -> new")
    public @NotNull ErrorMessage message() {
        return new ErrorMessage(this);
    }

    public static void registerAll() {
        for (TauErrors value : TauErrors.values()) {
            ServerErrorManager.instance().add(value);
        }
    }
}
