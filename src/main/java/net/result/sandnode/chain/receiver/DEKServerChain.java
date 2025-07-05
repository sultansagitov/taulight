package net.result.sandnode.chain.receiver;

import net.result.sandnode.chain.ReceiverChain;
import net.result.sandnode.chain.ServerChain;
import net.result.sandnode.db.*;
import net.result.sandnode.dto.DEKDTO;
import net.result.sandnode.exception.DatabaseException;
import net.result.sandnode.exception.SandnodeException;
import net.result.sandnode.exception.error.AddressedMemberNotFoundException;
import net.result.sandnode.exception.error.NotFoundException;
import net.result.sandnode.exception.error.UnauthorizedException;
import net.result.sandnode.message.Message;
import net.result.sandnode.message.RawMessage;
import net.result.sandnode.message.UUIDMessage;
import net.result.sandnode.message.types.DEKListMessage;
import net.result.sandnode.message.types.DEKRequest;
import net.result.sandnode.message.types.PublicKeyResponse;
import net.result.sandnode.message.util.Headers;
import net.result.sandnode.serverclient.Session;
import net.result.sandnode.util.JPAUtil;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

public class DEKServerChain extends ServerChain implements ReceiverChain {
    public DEKServerChain(Session session) {
        super(session);
    }

    @Override
    public Message handle(RawMessage raw) throws Exception {
        if (session.member == null) throw new UnauthorizedException();

        DEKRequest request = new DEKRequest(raw);
        DEKRequest.DataType type = request.type();
        DEKDTO dto = request.dto();

        return switch (type) {
            case SEND -> send(dto);
            case GET -> get(session.member);
            case GET_PERSONAL_KEY_OF -> getOf(dto);
        };
    }

    private @NotNull UUIDMessage send(DEKDTO dto) throws NotFoundException, DatabaseException {
        MemberRepository memberRepo = session.server.container.get(MemberRepository.class);
        EncryptedKeyRepository encryptedKeyRepo = session.server.container.get(EncryptedKeyRepository.class);
        KeyStorageRepository keyStorageRepo = session.server.container.get(KeyStorageRepository.class);


        MemberEntity receiver = memberRepo
                .findByNickname(dto.receiverNickname)
                .orElseThrow(NotFoundException::new);

        KeyStorageEntity encryptor = keyStorageRepo
                .find(dto.encryptorID)
                .orElseThrow(NotFoundException::new);

        EncryptedKeyEntity entity = encryptedKeyRepo.create(session.member, receiver, encryptor, dto.encryptedKey);

        return new UUIDMessage(new Headers(), entity);
    }

    private @NotNull DEKListMessage get(MemberEntity member) {
        JPAUtil jpaUtil = session.server.container.get(JPAUtil.class);

        session.member = jpaUtil.refresh(member);
        List<DEKDTO> list = session.member.encryptedKeys().stream().map(DEKDTO::new).toList();
        return new DEKListMessage(list);
    }

    private @NotNull PublicKeyResponse getOf(DEKDTO dto) throws SandnodeException {
        MemberRepository memberRepo = session.server.container.get(MemberRepository.class);

        Optional<KeyStorageEntity> opt = memberRepo.findPersonalKeyByNickname(dto.receiverNickname);
        KeyStorageEntity entity = opt.orElseThrow(AddressedMemberNotFoundException::new);

        return PublicKeyResponse.fromEntity(entity);
    }
}
