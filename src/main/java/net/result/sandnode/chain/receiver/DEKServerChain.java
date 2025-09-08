package net.result.sandnode.chain.receiver;

import net.result.sandnode.chain.ReceiverChain;
import net.result.sandnode.chain.ServerChain;
import net.result.sandnode.db.MemberUpdater;
import net.result.sandnode.dto.DEKRequestDTO;
import net.result.sandnode.entity.EncryptedKeyEntity;
import net.result.sandnode.entity.MemberEntity;
import net.result.sandnode.exception.error.AddressedMemberNotFoundException;
import net.result.sandnode.exception.error.NotFoundException;
import net.result.sandnode.exception.error.TooFewArgumentsException;
import net.result.sandnode.exception.error.UnauthorizedException;
import net.result.sandnode.message.Message;
import net.result.sandnode.message.RawMessage;
import net.result.sandnode.message.UUIDMessage;
import net.result.sandnode.message.types.DEKListMessage;
import net.result.sandnode.message.types.DEKRequest;
import net.result.sandnode.message.types.PublicKeyResponse;
import net.result.sandnode.message.util.Headers;
import net.result.sandnode.repository.EncryptedKeyRepository;
import net.result.sandnode.repository.MemberRepository;
import org.jetbrains.annotations.NotNull;

import java.util.stream.Collectors;

public class DEKServerChain extends ServerChain implements ReceiverChain {
    @Override
    public Message handle(RawMessage raw) {
        if (session.member == null) throw new UnauthorizedException();

        var request = new DEKRequest(raw);
        var dto = request.dto();

        if (dto.send != null) {
            return send(session.member, dto.send);
        } else if (dto.get) {
            return get(session.member);
        } else if (dto.getOf != null) {
            return getOf(session.member, dto);
        }

        throw new TooFewArgumentsException();
    }

    private @NotNull UUIDMessage send(MemberEntity you, DEKRequestDTO.Send sendDTO) {
        var memberRepo = session.server.container.get(MemberRepository.class);
        var encryptedKeyRepo = session.server.container.get(EncryptedKeyRepository.class);
        var memberUpdater = session.server.container.get(MemberUpdater.class);

        var receiver = memberRepo
                .findByNickname(sendDTO.receiverNickname)
                .orElseThrow(NotFoundException::new);

        var entity = encryptedKeyRepo.create(sendDTO.encryptedKey, you, receiver);

        memberUpdater.update(session);

        return new UUIDMessage(new Headers(), entity.id());
    }

    private @NotNull DEKListMessage get(MemberEntity you) {
        var list = you
                .getEncryptedKeys().stream()
                .map(EncryptedKeyEntity::toDEKResponseDTO)
                .collect(Collectors.toList());
        return new DEKListMessage((list));
    }

    private @NotNull PublicKeyResponse getOf(MemberEntity you, DEKRequestDTO dto) {
        MemberRepository memberRepo = session.server.container.get(MemberRepository.class);

        var entity = memberRepo
                .findPersonalKeyByNickname(dto.getOf)
                .orElseThrow(AddressedMemberNotFoundException::new);

        return entity.toDTO(you.getNickname());
    }
}
