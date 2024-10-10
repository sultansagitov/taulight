package net.result.sandnode.util.encryption.no;

import net.result.sandnode.util.encryption.interfaces.IGenerator;
import net.result.sandnode.util.encryption.interfaces.IKeyStorage;

public class NoGenerator implements IGenerator {

    private static final NoGenerator instance = new NoGenerator();

    public static NoGenerator getInstance() {
        return instance;
    }

    @Override
    public IKeyStorage generateKeyStorage() {
        return null;
    }

}
