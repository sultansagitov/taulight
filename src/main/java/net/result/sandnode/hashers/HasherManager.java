package net.result.sandnode.hashers;

import net.result.sandnode.exceptions.NoSuchHasherException;
import net.result.sandnode.util.Manager;

import static net.result.sandnode.hashers.Hashers.MD5;
import static net.result.sandnode.hashers.Hashers.SHA256;

public class HasherManager extends Manager<Hasher> {
    private static final HasherManager INSTANCE = new HasherManager();

    public static HasherManager instance() {
        return INSTANCE;
    }

    private HasherManager() {
        super();
        add(SHA256);
        add(MD5);
    }

    @Override
    protected void handleOverflow(Hasher hasher) {
        list.removeIf(h -> h.name().equals(hasher.name()));
    }

    public static Hasher find(String algorithm) throws NoSuchHasherException {
        for (Hasher hasher : instance().list) {
            if (hasher.name().equals(algorithm)) {
                return hasher;
            }
        }

        throw new NoSuchHasherException(algorithm);
    }
}
