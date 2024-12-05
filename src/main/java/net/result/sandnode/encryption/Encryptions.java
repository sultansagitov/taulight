package net.result.sandnode.encryption;

import net.result.sandnode.exceptions.CannotUseEncryption;
import net.result.sandnode.exceptions.NoSuchEncryptionException;
import net.result.sandnode.encryption.interfaces.IAsymmetricEncryption;
import net.result.sandnode.encryption.interfaces.IEncryption;
import net.result.sandnode.encryption.interfaces.ISymmetricEncryption;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static net.result.sandnode.encryption.AsymmetricEncryption.RSA;
import static net.result.sandnode.encryption.Encryption.NONE;
import static net.result.sandnode.encryption.SymmetricEncryption.AES;

public class Encryptions {
    public static final List<IEncryption> list = new ArrayList<>();

    static {
        Encryptions.register(RSA);
        Encryptions.register(NONE);
        Encryptions.register(AES);
    }

    public static void register(@NotNull IEncryption encryption) {
        list.removeIf((IEncryption e) ->
                e.asByte() == encryption.asByte()
                        || e.name().equals(encryption.name())
        );
        list.add(encryption);
    }

    public static @NotNull IEncryption find(byte e) throws NoSuchEncryptionException {
        for (IEncryption encryption : list)
            if (encryption.asByte() == e)
                return encryption;
        throw new NoSuchEncryptionException(e);
    }

    public static @NotNull IAsymmetricEncryption findAsymmetric(byte e) throws NoSuchEncryptionException, CannotUseEncryption {
        for (IEncryption encryption : list)
            if (encryption.asByte() == e)
                if (encryption.isAsymmetric()) {
                    return (IAsymmetricEncryption) encryption;
                } else {
                    throw new CannotUseEncryption(encryption);
                }
        throw new NoSuchEncryptionException(e);
    }

    public static @NotNull ISymmetricEncryption findSymmetric(byte e) throws NoSuchEncryptionException, CannotUseEncryption {
        for (IEncryption encryption : list)
            if (encryption.asByte() == e)
                if (encryption.isSymmetric()) {
                    return (ISymmetricEncryption) encryption;
                } else {
                    throw new CannotUseEncryption(encryption);
                }
        throw new NoSuchEncryptionException(e);
    }

    public static @NotNull IEncryption find(String e) throws NoSuchEncryptionException {
        for (IEncryption encryption : list)
            if (encryption.name().equalsIgnoreCase(e))
                return encryption;
        throw new NoSuchEncryptionException(e);
    }

    public static @NotNull IAsymmetricEncryption findAsymmetric(String e) throws NoSuchEncryptionException, CannotUseEncryption {
        for (IEncryption encryption : list)
            if (encryption.name().equalsIgnoreCase(e))
                if (encryption.isAsymmetric()) {
                    return (IAsymmetricEncryption) encryption;
                } else {
                    throw new CannotUseEncryption(e);
                }
        throw new NoSuchEncryptionException(e);
    }

    public static @NotNull ISymmetricEncryption findSymmetric(String e) throws NoSuchEncryptionException, CannotUseEncryption {
        for (IEncryption encryption : list)
            if (encryption.name().equalsIgnoreCase(e))
                if (encryption.isSymmetric()) {
                    return (ISymmetricEncryption) encryption;
                } else {
                    throw new CannotUseEncryption(e);
                }
        throw new NoSuchEncryptionException(e);
    }

    public static IAsymmetricEncryption[] getAsymmetric() {
        return list.stream()
                .filter(IAsymmetricEncryption.class::isInstance)
                .toArray(IAsymmetricEncryption[]::new);
    }

    public static ISymmetricEncryption[] getSymmetric() {
        return list.stream()
                .filter(ISymmetricEncryption.class::isInstance)
                .toArray(ISymmetricEncryption[]::new);
    }
}
