package net.result.sandnode.encryption;

import net.result.sandnode.exception.EncryptionTypeException;
import net.result.sandnode.exception.ImpossibleRuntimeException;
import net.result.sandnode.exception.NoSuchEncryptionException;
import net.result.sandnode.encryption.interfaces.AsymmetricEncryption;
import net.result.sandnode.encryption.interfaces.Encryption;
import net.result.sandnode.encryption.interfaces.SymmetricEncryption;
import net.result.sandnode.util.Manager;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.jetbrains.annotations.NotNull;

import java.security.Security;
import java.util.ArrayList;
import java.util.Collection;

public class EncryptionManager extends Manager<Encryption> {
    private static final EncryptionManager INSTANCE = new EncryptionManager();

    public static EncryptionManager instance() {
        return INSTANCE;
    }

    private EncryptionManager() {}

    public static void registerAll() {
        Security.addProvider(new BouncyCastleProvider());
        instance().add(AsymmetricEncryptions.RSA);
        instance().add(Encryptions.NONE);
        instance().add(SymmetricEncryptions.AES);
        instance().add(AsymmetricEncryptions.ECIES);
    }

    @Override
    protected void handleOverflow(Encryption encryption) {
        list.removeIf(e -> e.asByte() == encryption.asByte() || e.name().equals(encryption.name()));
    }

    public static @NotNull Encryption find(byte e) throws NoSuchEncryptionException {
        for (Encryption encryption : instance().list)
            if (encryption.asByte() == e)
                return encryption;
        throw new NoSuchEncryptionException(e);
    }

    public static @NotNull AsymmetricEncryption findAsymmetric(byte e)
            throws NoSuchEncryptionException, EncryptionTypeException {
        for (Encryption encryption : instance().list)
            if (encryption.asByte() == e)
                return encryption.asymmetric();
        throw new NoSuchEncryptionException(e);
    }

    public static @NotNull SymmetricEncryption findSymmetric(byte e)
            throws NoSuchEncryptionException, EncryptionTypeException {
        for (Encryption encryption : instance().list)
            if (encryption.asByte() == e)
                return encryption.symmetric();
        throw new NoSuchEncryptionException(e);
    }

    public static @NotNull Encryption find(String e) throws NoSuchEncryptionException {
        for (Encryption encryption : instance().list)
            if (encryption.name().equalsIgnoreCase(e))
                return encryption;
        throw new NoSuchEncryptionException(e);
    }

    public static @NotNull Collection<AsymmetricEncryption> getAsymmetric() {
        Collection<AsymmetricEncryption> result = new ArrayList<>();
        for (Encryption encryption : instance().list) {
            if (encryption instanceof AsymmetricEncryption) {
                try {
                    result.add(encryption.asymmetric());
                } catch (EncryptionTypeException e) {
                    throw new ImpossibleRuntimeException(e);
                }
            }
        }
        return result;
    }

    public static @NotNull Collection<SymmetricEncryption> getSymmetric() {
        Collection<SymmetricEncryption> result = new ArrayList<>();
        for (Encryption encryption : instance().list) {
            if (encryption instanceof SymmetricEncryption) {
                try {
                    result.add(encryption.symmetric());
                } catch (EncryptionTypeException e) {
                    throw new ImpossibleRuntimeException(e);
                }
            }
        }
        return result;
    }
}
