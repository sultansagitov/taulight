package net.result.sandnode.chain.receiver;

import net.result.sandnode.chain.ReceiverChain;
import net.result.sandnode.chain.ServerChain;
import net.result.sandnode.db.DBFileUtil;
import net.result.sandnode.db.JPAUtil;
import net.result.sandnode.dto.FileDTO;
import net.result.sandnode.entity.FileEntity;
import net.result.sandnode.entity.MemberEntity;
import net.result.sandnode.exception.error.*;
import net.result.sandnode.message.Message;
import net.result.sandnode.message.RawMessage;
import net.result.sandnode.message.UUIDMessage;
import net.result.sandnode.message.types.AvatarRequest;
import net.result.sandnode.message.util.Headers;
import net.result.sandnode.message.util.MessageTypes;
import net.result.sandnode.repository.MemberRepository;
import net.result.sandnode.util.FileIOUtil;
import org.jetbrains.annotations.Nullable;

public class AvatarServerChain extends ServerChain implements ReceiverChain {
    private DBFileUtil dbFileUtil;
    private MemberRepository memberRepo;

    @Override
    public @Nullable Message handle(RawMessage raw) {
        dbFileUtil = session.server.container.get(DBFileUtil.class);
        memberRepo = session.server.container.get(MemberRepository.class);

        AvatarRequest request = new AvatarRequest(raw);

        if (session.member == null) throw new UnauthorizedException();

        AvatarRequest.Type type = request.getType();

        return switch (type) {
            case SET -> set(session.member);
            case GET_MY -> getMy(session.member);
            case GET_OF -> getOf(request.getNickname());
        };
    }

    private UUIDMessage set(MemberEntity you) {
        JPAUtil jpaUtil = session.server.container.get(JPAUtil.class);

        FileDTO dto = FileIOUtil.receive(this::receive);

        if (!dto.contentType().startsWith("image/")) {
            throw new InvalidArgumentException();
        }

        FileEntity avatar = dbFileUtil.saveFile(dto, you.id().toString());

        if (!memberRepo.setAvatar(you, avatar)) {
            throw new ServerErrorException();
        }

        session.member = jpaUtil.refresh(you);

        return new UUIDMessage(new Headers().setType(MessageTypes.HAPPY), avatar.id());
    }

    @SuppressWarnings("SameReturnValue")
    private Message getMy(MemberEntity you) {
        FileEntity avatar = you.avatar();
        if (avatar == null) throw new NoEffectException();
        FileIOUtil.send(dbFileUtil.readImage(avatar), this::send);
        return null;
    }

    @SuppressWarnings("SameReturnValue")
    private Message getOf(String nickname) {
        MemberEntity member = memberRepo.findByNickname(nickname).orElseThrow(NotFoundException::new);
        FileEntity avatar = member.avatar();
        if (avatar == null) throw new NoEffectException();
        FileIOUtil.send(dbFileUtil.readImage(avatar), this::send);
        return null;
    }
}
