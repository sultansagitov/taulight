package net.result.sandnode.util.encryption.none;

import net.result.sandnode.util.encryption.interfaces.IGenerator;
import net.result.sandnode.util.encryption.interfaces.IKeyStorage;
import org.jetbrains.annotations.NotNull;

public class NoneGenerator implements IGenerator {

    private static final NoneGenerator INSTANCE = new NoneGenerator();

    public static NoneGenerator instance() {
        return INSTANCE;
    }

    @Override
    public @NotNull IKeyStorage generate() {
        return new NoneKeyStorage();
    }

}
