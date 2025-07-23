
package net.result.sandnode.chain;

import net.result.sandnode.GlobalTestState;
import net.result.sandnode.encryption.interfaces.AsymmetricEncryption;
import net.result.sandnode.encryption.interfaces.AsymmetricKeyStorage;
import net.result.sandnode.encryption.interfaces.SymmetricEncryption;
import net.result.sandnode.encryption.interfaces.SymmetricKeyStorage;
import net.result.sandnode.hubagent.ClientProtocol;
import net.result.sandnode.serverclient.SandnodeClient;
import net.result.sandnode.util.EncryptionUtil;
import net.result.sandnode.util.IOController;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class KeyExchangeTest {
    @BeforeAll
    public static void setup() throws Exception {
        GlobalTestState.initChainTests();
    }

    @Test
    void getPublicKey() throws Exception {
        SandnodeClient client = GlobalTestState.client;
        IOController io = client.io();
        {
            ClientProtocol.PUB(client);

            AsymmetricEncryption a = io.serverEncryption().asymmetric();
            AsymmetricKeyStorage expected = GlobalTestState.hubKeyStorage.asymmetricNonNull(a);
            AsymmetricKeyStorage actual = io.keyStorageRegistry.asymmetricNonNull(a);
            assertTrue(EncryptionUtil.isPublicKeysEquals(expected, actual));
        }

        {
            ClientProtocol.sendSYM(client);

            SymmetricEncryption a = io.symKeyEncryption().symmetric();
            SymmetricKeyStorage expected = GlobalTestState.session.io().keyStorageRegistry.symmetricNonNull(a);
            SymmetricKeyStorage actual = io.keyStorageRegistry.symmetricNonNull(a);
            assertEquals(expected.encoded(), actual.encoded());
            assertEquals(expected, actual);
        }
    }
}
