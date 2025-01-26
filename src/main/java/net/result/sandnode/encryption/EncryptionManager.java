package net.result.sandnode.encryption;

import net.result.sandnode.exception.EncryptionTypeException;
import net.result.sandnode.exception.ImpossibleRuntimeException;
import net.result.sandnode.exception.NoSuchEncryptionException;
import net.result.sandnode.encryption.interfaces.IAsymmetricEncryption;
import net.result.sandnode.encryption.interfaces.IEncryption;
import net.result.sandnode.encryption.interfaces.ISymmetricEncryption;
import net.result.sandnode.util.Manager;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.jetbrains.annotations.NotNull;

import java.security.Security;
import java.util.ArrayList;
import java.util.Collection;

import static net.result.sandnode.encryption.AsymmetricEncryption.ECIES;
import static net.result.sandnode.encryption.AsymmetricEncryption.RSA;
import static net.result.sandnode.encryption.Encryption.NONE;
import static net.result.sandnode.encryption.SymmetricEncryption.AES;

public class EncryptionManager extends Manager<IEncryption> {
    private static final EncryptionManager INSTANCE = new EncryptionManager();

    public static EncryptionManager instance() {
        return INSTANCE;
    }

    private EncryptionManager() {}

    public static void registerAll() {
        Security.addProvider(new BouncyCastleProvider());
        instance().add(RSA);
        instance().add(NONE);
        instance().add(AES);
        instance().add(ECIES);
    }

    @Override
    protected void handleOverflow(IEncryption encryption) {
        list.removeIf(e -> e.asByte() == encryption.asByte() || e.name().equals(encryption.name()));
    }

    public static @NotNull IEncryption find(byte e) throws NoSuchEncryptionException {
        for (IEncryption encryption : instance().list)
            if (encryption.asByte() == e)
                return encryption;
        throw new NoSuchEncryptionException(e);
    }

    public static @NotNull IAsymmetricEncryption findAsymmetric(byte e)
            throws NoSuchEncryptionException, EncryptionTypeException {
        for (IEncryption encryption : instance().list)
            if (encryption.asByte() == e)
                return encryption.asymmetric();
        throw new NoSuchEncryptionException(e);
    }

    public static @NotNull ISymmetricEncryption findSymmetric(byte e)
            throws NoSuchEncryptionException, EncryptionTypeException {
        for (IEncryption encryption : instance().list)
            if (encryption.asByte() == e)
                return encryption.symmetric();
        throw new NoSuchEncryptionException(e);
    }

    public static @NotNull IEncryption find(String e) throws NoSuchEncryptionException {
        for (IEncryption encryption : instance().list)
            if (encryption.name().equalsIgnoreCase(e))
                return encryption;
        throw new NoSuchEncryptionException(e);
    }

    public static @NotNull Collection<IAsymmetricEncryption> getAsymmetric() {
        Collection<IAsymmetricEncryption> result = new ArrayList<>();
        for (IEncryption encryption : instance().list) {
            if (encryption instanceof IAsymmetricEncryption) {
                try {
                    result.add(encryption.asymmetric());
                } catch (EncryptionTypeException e) {
                    throw new ImpossibleRuntimeException(e);
                }
            }
        }
        return result;
    }

    public static @NotNull Collection<ISymmetricEncryption> getSymmetric() {
        Collection<ISymmetricEncryption> result = new ArrayList<>();
        for (IEncryption encryption : instance().list) {
            if (encryption instanceof ISymmetricEncryption) {
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
