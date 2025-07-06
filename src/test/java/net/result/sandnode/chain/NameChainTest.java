package net.result.sandnode.chain;

import net.result.sandnode.GlobalTestState;
import net.result.sandnode.chain.sender.NameClientChain;
import net.result.sandnode.serverclient.SandnodeClient;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

public class NameChainTest {
    @BeforeAll
    public static void setup() throws Exception {
        GlobalTestState.initChainTests();
    }

    @Test
    void getName() {
        SandnodeClient client = GlobalTestState.client;
        try {
            NameClientChain chain = new NameClientChain(client);
            client.io.chainManager.linkChain(chain);
            String newName = chain.getName();
            client.io.chainManager.removeChain(chain);

            assertEquals(GlobalTestState.name, newName);
        } catch (Exception e) {
            fail(e);
        }
    }
}
