package net.result.sandnode.chain;

import net.result.sandnode.GlobalTestState;
import net.result.sandnode.chain.sender.HubInfoClientChain;
import net.result.sandnode.dto.HubInfoDTO;
import net.result.sandnode.serverclient.SandnodeClient;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

public class HubInfoChainTest {
    @BeforeAll
    public static void setup() throws IOException {
        GlobalTestState.initChainTests();
    }

    @Test
    void get() {
        SandnodeClient client = GlobalTestState.client;
        try {
            HubInfoClientChain chain = new HubInfoClientChain(client);
            client.io().chainManager.linkChain(chain);
            HubInfoDTO dto = chain.get(true, false);
            client.io().chainManager.removeChain(chain);

            String newName = dto.name();
            assertEquals(GlobalTestState.name, newName);
        } catch (Exception e) {
            fail(e);
        }
    }
}
