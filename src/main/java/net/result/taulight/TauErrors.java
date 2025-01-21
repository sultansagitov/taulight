package net.result.taulight;

import net.result.sandnode.messages.types.ErrorMessage;
import net.result.sandnode.server.SandnodeError;
import net.result.sandnode.server.ServerErrorManager;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public enum TauErrors implements SandnodeError {
    CHAT_NOT_FOUND(4000, "Chat not found");

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
