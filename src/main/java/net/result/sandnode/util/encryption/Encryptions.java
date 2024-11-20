package net.result.sandnode.util.encryption;

import net.result.sandnode.exceptions.encryption.CannotUseEncryption;
import net.result.sandnode.exceptions.encryption.NoSuchEncryptionException;
import net.result.sandnode.util.encryption.interfaces.IAsymmetricEncryption;
import net.result.sandnode.util.encryption.interfaces.IEncryption;
import net.result.sandnode.util.encryption.interfaces.ISymmetricEncryption;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static net.result.sandnode.util.encryption.AsymmetricEncryption.RSA;
import static net.result.sandnode.util.encryption.Encryption.NONE;
import static net.result.sandnode.util.encryption.SymmetricEncryption.AES;

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

}
