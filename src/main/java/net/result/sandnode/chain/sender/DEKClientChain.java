package net.result.sandnode.chain.sender;

import net.result.sandnode.chain.ClientChain;
import net.result.sandnode.dto.DEKResponseDTO;
import net.result.sandnode.dto.KeyDTO;
import net.result.sandnode.encryption.interfaces.KeyStorage;
import net.result.sandnode.key.DEKServerSource;
import net.result.sandnode.key.ServerSource;
import net.result.sandnode.key.Source;
import net.result.sandnode.message.UUIDMessage;
import net.result.sandnode.message.types.DEKListMessage;
import net.result.sandnode.message.types.DEKRequest;
import net.result.sandnode.message.types.PublicKeyResponse;
import net.result.sandnode.serverclient.SandnodeClient;
import net.result.sandnode.util.DEKUtil;
import net.result.sandnode.util.Member;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.UUID;

public class DEKClientChain extends ClientChain {
    public DEKClientChain(SandnodeClient client) {
        super(client);
    }

    public UUID sendDEK(@NotNull Source source, String receiver, KeyStorage encryptor, KeyStorage dek) {
        var raw = sendAndReceive(DEKRequest.send(receiver, encryptor, dek));
        var keyID = new UUIDMessage(raw).uuid;

        client.node().agent().config.saveDEK(source, new Member(client), new Member(receiver, client.address), keyID, dek);

        return keyID;
    }

    public Collection<DEKResponseDTO> get() {
        var raw = sendAndReceive(DEKRequest.get());
        var keys = new DEKListMessage(raw).list();

        var m1 = new Member(client);
        for (DEKResponseDTO key : keys) {
            var m2 = new Member(key.senderNickname, client.address);
            var agent = client.node().agent();
            var personalKey = agent.config.loadPersonalKey(new Member(client.nickname, client.address));
            var decrypted = DEKUtil.decrypt(key.dek.encryptedKey, personalKey);
            agent.config.saveDEK(new DEKServerSource(client), m1, m2, key.dek.id, decrypted);
        }

        return keys;
    }

    public KeyDTO getKeyOf(String nickname) {
        var raw = sendAndReceive(DEKRequest.getPersonalKeyOf(nickname));
        var key = PublicKeyResponse.getKeyDTO(raw);

        client.node().agent().config.saveEncryptor(
                new ServerSource(client.address),
                new Member(nickname, client.address),
                key.keyStorage()
        );

        return key;
    }
}
