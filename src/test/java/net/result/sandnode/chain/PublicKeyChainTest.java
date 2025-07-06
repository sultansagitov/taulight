
package net.result.sandnode.chain;

import net.result.sandnode.GlobalTestState;
import net.result.sandnode.chain.sender.PublicKeyClientChain;
import net.result.sandnode.encryption.AsymmetricEncryptions;
import net.result.sandnode.encryption.ecies.ECIESKeyStorage;
import net.result.sandnode.serverclient.SandnodeClient;
import net.result.sandnode.util.EncryptionUtil;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class PublicKeyChainTest {
    @BeforeAll
    public static void setup() throws Exception {
        GlobalTestState.initChainTests();
    }

    @Test
    void getPublicKey() {
        SandnodeClient client = GlobalTestState.client;
        try {
            PublicKeyClientChain chain = new PublicKeyClientChain(client);
            client.io.chainManager.linkChain(chain);
            chain.getPublicKey();
            client.io.chainManager.removeChain(chain);

            AsymmetricEncryptions a = AsymmetricEncryptions.ECIES;
            ECIESKeyStorage expected = (ECIESKeyStorage) GlobalTestState.hubKeyStorage.get(a).orElseThrow();
            ECIESKeyStorage actual = (ECIESKeyStorage) client.io.keyStorageRegistry.get(a).orElseThrow();
            assertTrue(EncryptionUtil.isPublicKeysEquals(expected, actual));
        } catch (Exception e) {
            fail(e);
        }
    }
}
