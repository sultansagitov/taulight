package net.result.taulight.error;

import net.result.sandnode.error.SandnodeError;
import net.result.sandnode.error.ServerErrorManager;
import net.result.sandnode.exception.error.SandnodeErrorException;
import net.result.taulight.exception.error.PermissionDeniedException;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.function.Supplier;

public enum TauErrors implements SandnodeError {
    PERMISSION_DENIED("Permission denied", PermissionDeniedException::new);

    private final String code;
    private final String desc;
    private final Supplier<SandnodeErrorException> exceptionSupplier;

    TauErrors(@NotNull String desc, Supplier<SandnodeErrorException> exceptionSupplier) {
        this.code = "taulight:%s".formatted(name().toLowerCase());
        this.desc = desc;
        this.exceptionSupplier = exceptionSupplier;
    }

    @Override
    public String code() {
        return code;
    }

    @Override
    public String description() {
        return desc;
    }

    @Override
    @Contract(" -> new")
    public SandnodeErrorException exception() {
        return exceptionSupplier.get();
    }

    public static void registerAll() {
        ServerErrorManager instance = ServerErrorManager.instance();
        Arrays.stream(TauErrors.values()).forEach(instance::add);
    }
}
