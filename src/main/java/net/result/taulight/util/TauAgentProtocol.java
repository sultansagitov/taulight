package net.result.taulight.util;

import net.result.sandnode.chain.sender.DEKClientChain;
import net.result.sandnode.config.KeyEntry;
import net.result.sandnode.encryption.interfaces.KeyStorage;
import net.result.sandnode.exception.error.KeyStorageNotFoundException;
import net.result.sandnode.hubagent.Agent;
import net.result.sandnode.serverclient.SandnodeClient;
import net.result.sandnode.util.Member;

import java.util.UUID;

public class TauAgentProtocol {
    public static KeyStorage loadDEK(SandnodeClient client, UUID keyID) {
        Agent agent = client.node().agent();
        try {
            return agent.config.loadDEK(client.address, keyID);
        } catch (KeyStorageNotFoundException e) {
            DEKClientChain chain = new DEKClientChain(client);
            client.io().chainManager.linkChain(chain);
            chain.get();
            client.io().chainManager.removeChain(chain);
            return agent.config.loadDEK(client.address, keyID);
        }
    }

    public static KeyEntry loadDEK(SandnodeClient client, String other) {
        Agent agent = client.node().agent();
        Member member = new Member(other, client.address);
        try {
            return agent.config.loadDEK(member);
        } catch (KeyStorageNotFoundException e) {
            DEKClientChain chain = new DEKClientChain(client);
            client.io().chainManager.linkChain(chain);
            chain.get();
            client.io().chainManager.removeChain(chain);
            return agent.config.loadDEK(member);
        }
    }
}
