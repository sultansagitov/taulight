package net.result.sandnode.util.encryption.core.none;

import net.result.sandnode.util.encryption.core.interfaces.IGenerator;
import net.result.sandnode.util.encryption.core.interfaces.IKeyStorage;
import org.jetbrains.annotations.NotNull;

public class NoneGenerator implements IGenerator {

    private static final NoneGenerator INSTANCE = new NoneGenerator();

    public static NoneGenerator instance() {
        return INSTANCE;
    }

    @Override
    public @NotNull IKeyStorage generateKeyStorage() {
        return new NoneKeyStorage();
    }

}
