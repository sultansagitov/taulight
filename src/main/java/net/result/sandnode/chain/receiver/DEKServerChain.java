package net.result.sandnode.chain.receiver;

import net.result.sandnode.chain.ReceiverChain;
import net.result.sandnode.chain.ServerChain;
import net.result.sandnode.db.*;
import net.result.sandnode.exception.error.AddressedMemberNotFoundException;
import net.result.sandnode.exception.error.NotFoundException;
import net.result.sandnode.exception.error.UnauthorizedException;
import net.result.sandnode.message.UUIDMessage;
import net.result.sandnode.message.types.DEKListMessage;
import net.result.sandnode.message.types.PublicKeyResponse;
import net.result.sandnode.message.util.Headers;
import net.result.sandnode.serverclient.Session;
import net.result.sandnode.util.JPAUtil;
import net.result.sandnode.dto.DEKDTO;
import net.result.sandnode.message.types.DEKRequest;

import java.util.List;
import java.util.Optional;

public class DEKServerChain extends ServerChain implements ReceiverChain {
    public DEKServerChain(Session session) {
        super(session);
    }

    @Override
    public void sync() throws Exception {
        if (session.member == null) throw new UnauthorizedException();

        JPAUtil jpaUtil = session.server.container.get(JPAUtil.class);
        MemberRepository memberRepo = session.server.container.get(MemberRepository.class);
        EncryptedKeyRepository encryptedKeyRepo = session.server.container.get(EncryptedKeyRepository.class);
        KeyStorageRepository keyStorageRepo = session.server.container.get(KeyStorageRepository.class);

        DEKRequest request = new DEKRequest(queue.take());
        DEKRequest.DataType type = request.type();
        DEKDTO dto = request.dto();

        switch (type) {
            case SEND -> {
                MemberEntity receiver = memberRepo
                        .findByNickname(dto.receiverNickname)
                        .orElseThrow(NotFoundException::new);

                KeyStorageEntity encryptor = keyStorageRepo
                        .find(dto.encryptorID)
                        .orElseThrow(NotFoundException::new);

                EncryptedKeyEntity entity = encryptedKeyRepo.create(session.member, receiver, encryptor, dto.encryptedKey);

                sendFin(new UUIDMessage(new Headers(), entity));
            }
            case GET -> {
                session.member = jpaUtil.refresh(session.member);
                List<DEKDTO> list = session.member.encryptedKeys().stream().map(DEKDTO::new).toList();
                sendFin(new DEKListMessage(list));
            }
            case GET_PERSONAL_KEY_OF -> {
                Optional<KeyStorageEntity> opt = memberRepo.findPersonalKeyByNickname(dto.receiverNickname);
                KeyStorageEntity entity = opt.orElseThrow(AddressedMemberNotFoundException::new);

                sendFin(PublicKeyResponse.fromEntity(entity));
            }
        }
    }
}
