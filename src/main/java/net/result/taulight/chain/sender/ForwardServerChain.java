package net.result.taulight.chain.sender;

import net.result.sandnode.chain.ServerChain;
import net.result.sandnode.db.EncryptedKeyEntity;
import net.result.sandnode.db.EncryptedKeyRepository;
import net.result.sandnode.dto.DEKDTO;
import net.result.sandnode.exception.DatabaseException;
import net.result.sandnode.exception.ExpectedMessageException;
import net.result.sandnode.exception.UnknownSandnodeErrorException;
import net.result.sandnode.exception.UnprocessedMessagesException;
import net.result.sandnode.exception.error.KeyStorageNotFoundException;
import net.result.sandnode.exception.error.SandnodeErrorException;
import net.result.sandnode.exception.error.ServerErrorException;
import net.result.sandnode.message.types.DEKListMessage;
import net.result.sandnode.message.types.HappyMessage;
import net.result.sandnode.message.util.MessageTypes;
import net.result.sandnode.serverclient.Session;
import net.result.taulight.message.types.ForwardResponse;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class ForwardServerChain extends ServerChain {
    public ForwardServerChain(Session session) {
        super(session);
    }

    public synchronized void response(ForwardResponse res) throws UnprocessedMessagesException, InterruptedException,
            ExpectedMessageException, UnknownSandnodeErrorException, SandnodeErrorException, DatabaseException {
        send(res);

        try {
            new HappyMessage(receive());
        } catch (KeyStorageNotFoundException e) {
            EncryptedKeyRepository encryptedKeyRepo = session.server.container.get(EncryptedKeyRepository.class);

            UUID keyID = res.getServerMessage().message.keyID;
            Optional<EncryptedKeyEntity> opt = encryptedKeyRepo.find(keyID);
            EncryptedKeyEntity entity = opt.orElseThrow(ServerErrorException::new);
            sendAndReceive(new DEKListMessage(List.of(new DEKDTO(entity)))).expect(MessageTypes.HAPPY);
        }
    }
}

