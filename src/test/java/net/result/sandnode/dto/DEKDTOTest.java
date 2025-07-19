package net.result.sandnode.dto;

import net.result.sandnode.encryption.AsymmetricEncryptions;
import net.result.sandnode.encryption.EncryptionManager;
import net.result.sandnode.encryption.SymmetricEncryptions;
import net.result.sandnode.encryption.interfaces.AsymmetricKeyStorage;
import net.result.sandnode.encryption.interfaces.KeyStorage;
import net.result.sandnode.encryption.interfaces.SymmetricKeyStorage;
import org.junit.jupiter.api.*;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class DEKDTOTest {
    private static UUID encryptorID;
    private static AsymmetricKeyStorage encryptor;
    private static SymmetricKeyStorage keyStorage;

    @BeforeAll
    public static void setup() {
        EncryptionManager.registerAll();

        encryptorID = UUID.randomUUID();
        encryptor = AsymmetricEncryptions.ECIES.generate();
        keyStorage = SymmetricEncryptions.AES.generate();
    }

    @Test
    void testDEKDTOEncryptDecrypt() throws Exception {
        KeyDTO encryptorDTO = new KeyDTO(encryptorID, encryptor);

        DEKDTO dekDTO = new DEKDTO("receiver", encryptorDTO, keyStorage);

        assertNotNull(dekDTO.encryptedKey);

        KeyStorage decrypted = dekDTO.decrypt(encryptor);
        Assertions.assertEquals(keyStorage.encryption().name(), decrypted.encryption().name());

        if (decrypted.encryption().isAsymmetric()) {
            String originalEncoded = keyStorage.asymmetric().encodedPublicKey();
            String decryptedEncoded = decrypted.asymmetric().encodedPublicKey();
            assertEquals(originalEncoded, decryptedEncoded);
        } else {
            Assertions.assertEquals(keyStorage.symmetric().encoded(), decrypted.symmetric().encoded());
        }
    }
}
