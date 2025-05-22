package net.result.sandnode.chain.receiver;

import net.result.sandnode.chain.ReceiverChain;
import net.result.sandnode.chain.ServerChain;
import net.result.sandnode.db.KeyStorageEntity;
import net.result.sandnode.db.KeyStorageRepository;
import net.result.sandnode.db.MemberEntity;
import net.result.sandnode.db.MemberRepository;
import net.result.sandnode.exception.error.NotFoundException;
import net.result.sandnode.exception.error.UnauthorizedException;
import net.result.sandnode.message.types.HappyMessage;
import net.result.sandnode.serverclient.Session;
import net.result.sandnode.db.EncryptedKeyRepository;
import net.result.taulight.dto.PersonalKeyDTO;
import net.result.taulight.message.types.PersonalKeyMessage;

public class PersonalKeyServerChain extends ServerChain implements ReceiverChain {
    public PersonalKeyServerChain(Session session) {
        super(session);
    }

    @Override
    public void sync() throws Exception {
        if (session.member == null) throw new UnauthorizedException();

        MemberRepository memberRepo = session.server.container.get(MemberRepository.class);
        EncryptedKeyRepository encryptedKeyRepo = session.server.container.get(EncryptedKeyRepository.class);
        KeyStorageRepository keyStorageRepo = session.server.container.get(KeyStorageRepository.class);

        PersonalKeyDTO dto = new PersonalKeyMessage(queue.take()).dto();

        MemberEntity receiver = memberRepo.findByNickname(dto.nickname).orElseThrow(NotFoundException::new);
        KeyStorageEntity encryptor = keyStorageRepo.find(dto.encryptorID).orElseThrow(NotFoundException::new);

        encryptedKeyRepo.create(session.member, receiver, encryptor, dto.encryptedKey);

        send(new HappyMessage());
    }
}
