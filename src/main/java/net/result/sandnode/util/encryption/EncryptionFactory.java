package net.result.sandnode.util.encryption;

import net.result.sandnode.util.encryption.aes.AESDecryptor;
import net.result.sandnode.util.encryption.aes.AESEncryptor;
import net.result.sandnode.util.encryption.aes.AESGenerator;
import net.result.sandnode.util.encryption.asymmetric.rsa.RSAKeyStorage;
import net.result.sandnode.util.encryption.interfaces.IDecryptor;
import net.result.sandnode.util.encryption.interfaces.IEncryptor;
import net.result.sandnode.util.encryption.interfaces.IGenerator;
import net.result.sandnode.util.encryption.interfaces.IKeyStorage;
import net.result.sandnode.util.encryption.no.NoDecryptor;
import net.result.sandnode.util.encryption.no.NoEncryptor;
import net.result.sandnode.util.encryption.no.NoGenerator;
import net.result.sandnode.util.encryption.rsa.RSADecryptor;
import net.result.sandnode.util.encryption.rsa.RSAEncryptor;
import net.result.sandnode.util.encryption.rsa.RSAGenerator;
import net.result.sandnode.util.encryption.symmetric.aes.AESKeyStorage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

// <editor-fold desc="Encryption Setup Tutorial - Adding a New Encryption Algorithm (NewCrypto)">
//
// This guide explains how to add a new encryption algorithm (NewCrypto) to the system, including both symmetric and asymmetric types.
//
// 1. Create Key Storage Classes:
//    - Symmetric: Create `NewCryptoKeyStorage.java` implementing `SymmetricKeyStorage`.
//    - Asymmetric: Create `NewCryptoKeyStorage.java` implementing `AsymmetricKeyStorage`.
//
// 2. Implement IGenerator:
//    - Symmetric: Implement `NewCryptoGenerator.java` extending `ISymmetricGenerator`.
//    - Asymmetric: Implement `NewCryptoGenerator.java` extending `IAsymmetricGenerator`.
//
// 3. Implement Encryption/Decryption:
//    - Symmetric:
//      - `NewCryptoEncryptor.java` implements `IEncryptor`.
//      - `NewCryptoDecryptor.java` implements `IDecryptor`.
//    - Asymmetric:
//      - `NewCryptoEncryptor.java` implements `IEncryptor`.
//      - `NewCryptoDecryptor.java` implements `IDecryptor`.
//
// 4. Add Key Converters (Optional for PEM conversion):
//    - Symmetric: Create `NewCryptoKeyConvertor.java` implementing `ISymmetricKeyConvertor`.
//    - Asymmetric: Create `NewCryptoPrivateKeyConvertor.java` & `NewCryptoPublicKeyConvertor.java` implementing `IAsymmetricConvertor`.
//
// 5. Add to Factories:
//    - Symmetric: Register `NewCryptoGenerator`, `NewCryptoEncryptor`, `NewCryptoDecryptor` in `SymmetricEncryptionFactory.java`.
//    - Asymmetric: Register `NewCryptoGenerator`, `NewCryptoEncryptor`, `NewCryptoDecryptor` in `AsymmetricEncryptionFactory.java`.
//
// 6. Update EncryptionFactory:
//    - Symmetric: Add a case for "NEWCRYPTO" in the `SymmetricEncryptionFactory.getGenerator()` method to support your new algorithm.
//    - Asymmetric: Add a case for "NEWCRYPTO" in the `AsymmetricEncryptionFactory.getGenerator()` method to support your new algorithm.
//
// After following these steps, NewCrypto will be fully integrated into the system.
//
// </editor-fold>

public class EncryptionFactory {
    private static final Logger LOGGER = LogManager.getLogger(EncryptionFactory.class);

    public static @NotNull IGenerator getGenerator(@NotNull Encryption encryption) {
        return switch (encryption) {
            case RSA -> new RSAGenerator();
            case AES -> new AESGenerator();
            case NO -> new NoGenerator();
        };
    }

    public static @NotNull IEncryptor getEncryptor(@NotNull Encryption encryption) {
        return switch (encryption) {
            case RSA -> new RSAEncryptor();
            case AES -> new AESEncryptor();
            case NO -> new NoEncryptor();
        };
    }

    public static @NotNull IDecryptor getDecryptor(@NotNull Encryption encryption) {
        return switch (encryption) {
            case RSA -> new RSADecryptor();
            case AES -> new AESDecryptor();
            case NO -> new NoDecryptor();
        };
    }

    public static @Nullable IKeyStorage getKeyStorage(
            @NotNull GlobalKeyStorage globalKeyStorage,
            @NotNull Encryption encryption
    ) {
        return switch (encryption) {
            case RSA -> new RSAKeyStorage(globalKeyStorage);
            case AES -> new AESKeyStorage(globalKeyStorage);
            case NO -> null;
        };
    }

    public static void setKeyStorage(
            @NotNull GlobalKeyStorage globalKeyStorage,
            @NotNull Encryption encryption,
            @NotNull IKeyStorage keyStorage
    ) {
        switch (encryption) {
            case RSA: {
                if (keyStorage instanceof RSAKeyStorage) {
                    globalKeyStorage.setRSAKeyStorage((RSAKeyStorage) keyStorage);
                }
                break;
            }
            case AES: {
                if (keyStorage instanceof AESKeyStorage) {
                    globalKeyStorage.setAESKeyStorage((AESKeyStorage) keyStorage);
                }
                break;
            }
        }
    }
}
