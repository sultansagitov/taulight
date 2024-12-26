package net.result.sandnode.hashers;

import net.result.sandnode.exceptions.NoSuchHasherException;
import net.result.sandnode.util.Manager;

import static net.result.sandnode.hashers.Hasher.MD5;
import static net.result.sandnode.hashers.Hasher.SHA256;

public class Hashers extends Manager<IHasher> {
    private static final Hashers INSTANCE = new Hashers();

    public static Hashers instance() {
        return INSTANCE;
    }

    private Hashers() {
        super();
        add(SHA256);
        add(MD5);
    }

    @Override
    protected void handleOverflow(IHasher hasher) {
        list.removeIf(h -> h.name().equals(hasher.name()));
    }

    public static IHasher find(String algorithm) throws NoSuchHasherException {
        for (IHasher hasher : instance().list) {
            if (hasher.name().equals(algorithm)) {
                return hasher;
            }
        }

        throw new NoSuchHasherException(algorithm);
    }
}
