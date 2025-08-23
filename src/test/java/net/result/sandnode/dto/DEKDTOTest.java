package net.result.sandnode.dto;

import net.result.sandnode.encryption.AsymmetricEncryptions;
import net.result.sandnode.encryption.EncryptionManager;
import net.result.sandnode.encryption.SymmetricEncryptions;
import net.result.sandnode.encryption.interfaces.AsymmetricKeyStorage;
import net.result.sandnode.encryption.interfaces.KeyStorage;
import net.result.sandnode.encryption.interfaces.SymmetricKeyStorage;
import net.result.sandnode.util.DEKUtil;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

public class DEKDTOTest {
    private static String sender;
    private static AsymmetricKeyStorage encryptor;
    private static SymmetricKeyStorage keyStorage;

    @BeforeAll
    public static void setup() {
        EncryptionManager.registerAll();

        sender = "rizl";
        encryptor = AsymmetricEncryptions.ECIES.generate();
        keyStorage = SymmetricEncryptions.AES.generate();
    }

    @Test
    void testDEKDTOEncryptDecrypt() {
        KeyDTO encryptorDTO = new KeyDTO(sender, encryptor);

        DEKDTO dekDTO = new DEKDTO();
        dekDTO.encryptedKey = DEKUtil.getEncrypted(encryptorDTO.keyStorage(), keyStorage);

        assertNotNull(dekDTO.encryptedKey);

        KeyStorage decrypted = DEKUtil.decrypt(dekDTO.encryptedKey, encryptor);
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
