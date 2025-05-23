package net.result.sandnode.chain.receiver;

import net.result.sandnode.chain.ReceiverChain;
import net.result.sandnode.chain.ServerChain;
import net.result.sandnode.db.*;
import net.result.sandnode.encryption.interfaces.AsymmetricKeyStorage;
import net.result.sandnode.encryption.interfaces.Encryption;
import net.result.sandnode.exception.error.AddressedMemberNotFoundException;
import net.result.sandnode.exception.error.NotFoundException;
import net.result.sandnode.exception.error.UnauthorizedException;
import net.result.sandnode.message.UUIDMessage;
import net.result.sandnode.message.types.PersonalKeyListMessage;
import net.result.sandnode.message.types.PublicKeyResponse;
import net.result.sandnode.message.util.Headers;
import net.result.sandnode.serverclient.Session;
import net.result.sandnode.util.JPAUtil;
import net.result.taulight.dto.PersonalKeyDTO;
import net.result.taulight.message.types.PersonalKeyRequest;

import java.util.List;
import java.util.Optional;

public class PersonalKeyServerChain extends ServerChain implements ReceiverChain {
    public PersonalKeyServerChain(Session session) {
        super(session);
    }

    @Override
    public void sync() throws Exception {
        if (session.member == null) throw new UnauthorizedException();

        JPAUtil jpaUtil = session.server.container.get(JPAUtil.class);
        MemberRepository memberRepo = session.server.container.get(MemberRepository.class);
        EncryptedKeyRepository encryptedKeyRepo = session.server.container.get(EncryptedKeyRepository.class);
        KeyStorageRepository keyStorageRepo = session.server.container.get(KeyStorageRepository.class);

        PersonalKeyRequest request = new PersonalKeyRequest(queue.take());
        PersonalKeyRequest.DataType type = request.type();
        PersonalKeyDTO dto = request.dto();

        switch (type) {
            case SEND_PERSONAL_KEY -> {
                MemberEntity receiver = memberRepo.findByNickname(dto.receiverNickname).orElseThrow(NotFoundException::new);
                KeyStorageEntity encryptor = keyStorageRepo.find(dto.encryptorID).orElseThrow(NotFoundException::new);

                EncryptedKeyEntity entity = encryptedKeyRepo.create(session.member, receiver, encryptor, dto.encryptedKey);

                sendFin(new UUIDMessage(new Headers(), entity));
            }
            case GET_KEYS -> {
                session.member = jpaUtil.refresh(session.member);
                List<PersonalKeyDTO> list = session.member.encryptedKeys().stream().map(PersonalKeyDTO::new).toList();
                sendFin(new PersonalKeyListMessage(list));
            }
            case GET_KEY_OF -> {
                // TODO make with one db request
                Optional<MemberEntity> opt = memberRepo.findByNickname(dto.receiverNickname);
                MemberEntity member = opt.orElseThrow(AddressedMemberNotFoundException::new);
                KeyStorageEntity entity = member.publicKey();

                // TODO move logic
                Encryption encryption = entity.encryption();
                AsymmetricKeyStorage keyStorage = encryption.asymmetric()
                        .publicKeyConvertor()
                        .toKeyStorage(entity.encodedKey());

                sendFin(new PublicKeyResponse(new Headers().setValue("id", entity.id().toString()), keyStorage));
            }
        }
    }
}
