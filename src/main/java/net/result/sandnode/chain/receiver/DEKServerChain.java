package net.result.sandnode.chain.receiver;

import net.result.sandnode.chain.ReceiverChain;
import net.result.sandnode.chain.ServerChain;
import net.result.sandnode.db.*;
import net.result.sandnode.dto.DEKRequestDTO;
import net.result.sandnode.dto.DEKResponseDTO;
import net.result.sandnode.exception.DatabaseException;
import net.result.sandnode.exception.SandnodeException;
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
import net.result.sandnode.serverclient.Session;
import net.result.sandnode.util.JPAUtil;
import org.jetbrains.annotations.NotNull;

import java.util.stream.Collectors;

public class DEKServerChain extends ServerChain implements ReceiverChain {
    public DEKServerChain(Session session) {
        super(session);
    }

    @Override
    public Message handle(RawMessage raw) throws Exception {
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

    private @NotNull UUIDMessage send(MemberEntity you, DEKRequestDTO.Send sendDTO) throws NotFoundException, DatabaseException {
        var memberRepo = session.server.container.get(MemberRepository.class);
        var encryptedKeyRepo = session.server.container.get(EncryptedKeyRepository.class);

        var receiver = memberRepo
                .findByNickname(sendDTO.receiverNickname)
                .orElseThrow(NotFoundException::new);

        var entity = encryptedKeyRepo.create(you, receiver, sendDTO.encryptedKey);

        return new UUIDMessage(new Headers(), entity);
    }

    private @NotNull DEKListMessage get(MemberEntity you) throws DatabaseException {
        JPAUtil jpaUtil = session.server.container.get(JPAUtil.class);

        session.member = jpaUtil.refresh(you);
        var list = session.member
                .encryptedKeys().stream()
                .map(DEKResponseDTO::new)
                .collect(Collectors.toList());
        return new DEKListMessage((list));
    }

    private @NotNull PublicKeyResponse getOf(MemberEntity you, DEKRequestDTO dto) throws SandnodeException {
        MemberRepository memberRepo = session.server.container.get(MemberRepository.class);

        var entity = memberRepo
                .findPersonalKeyByNickname(dto.getOf)
                .orElseThrow(AddressedMemberNotFoundException::new);

        return PublicKeyResponse.fromEntity(you.nickname(), entity);
    }
}
