package net.result.taulight.util;

import net.result.sandnode.chain.sender.DEKClientChain;
import net.result.sandnode.config.AgentConfig;
import net.result.sandnode.config.KeyEntry;
import net.result.sandnode.encryption.interfaces.KeyStorage;
import net.result.sandnode.exception.error.KeyStorageNotFoundException;
import net.result.sandnode.serverclient.SandnodeClient;
import net.result.sandnode.util.Member;

import java.util.UUID;

public class TauAgentProtocol {
    public static KeyStorage loadDEK(SandnodeClient client, UUID keyID) {
        AgentConfig config = client.node().agent().config;
        try {
            return config.loadDEK(keyID);
        } catch (KeyStorageNotFoundException e) {
            DEKClientChain chain = new DEKClientChain(client);
            client.io().chainManager.linkChain(chain);
            chain.get();
            client.io().chainManager.removeChain(chain);
            return config.loadDEK(keyID);
        }
    }

    public static KeyEntry loadDEK(SandnodeClient client, String other) {
        AgentConfig config = client.node().agent().config;
        Member clientMember = new Member(client);
        Member member = new Member(other, client.address);
        try {
            return config.loadDEK(clientMember, member);
        } catch (KeyStorageNotFoundException e) {
            DEKClientChain chain = new DEKClientChain(client);
            client.io().chainManager.linkChain(chain);
            chain.get();
            client.io().chainManager.removeChain(chain);
            return config.loadDEK(clientMember, member);
        }
    }
}
