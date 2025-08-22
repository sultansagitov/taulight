package net.result.sandnode.hasher;

import net.result.sandnode.exception.NoSuchHasherException;
import net.result.sandnode.util.Manager;

public class HasherManager extends Manager<Hasher> {
    private static final HasherManager INSTANCE = new HasherManager();

    public static HasherManager instance() {
        return INSTANCE;
    }

    private HasherManager() {
        add(Hashers.SHA256);
        add(Hashers.MD5);
    }

    @Override
    protected void handleOverflow(Hasher hasher) {
        list.removeIf(h -> h.name().equals(hasher.name()));
    }

    public static Hasher find(String algorithm) {
        for (Hasher hasher : instance().list) {
            if (hasher.name().equals(algorithm)) {
                return hasher;
            }
        }

        throw new NoSuchHasherException(algorithm);
    }
}
