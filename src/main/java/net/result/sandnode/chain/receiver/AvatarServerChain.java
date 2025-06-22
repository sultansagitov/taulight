package net.result.sandnode.chain.receiver;

import net.result.sandnode.chain.ReceiverChain;
import net.result.sandnode.chain.ServerChain;
import net.result.sandnode.db.FileEntity;
import net.result.sandnode.db.MemberEntity;
import net.result.sandnode.db.MemberRepository;
import net.result.sandnode.dto.FileDTO;
import net.result.sandnode.exception.error.*;
import net.result.sandnode.message.IMessage;
import net.result.sandnode.message.RawMessage;
import net.result.sandnode.message.UUIDMessage;
import net.result.sandnode.message.types.AvatarRequest;
import net.result.sandnode.message.types.FileMessage;
import net.result.sandnode.message.util.Headers;
import net.result.sandnode.message.util.MessageTypes;
import net.result.sandnode.serverclient.Session;
import net.result.sandnode.util.DBFileUtil;
import net.result.sandnode.util.JPAUtil;

public class AvatarServerChain extends ServerChain implements ReceiverChain {
    private final DBFileUtil dbFileUtil = session.server.container.get(DBFileUtil.class);
    private final MemberRepository memberRepo = session.server.container.get(MemberRepository.class);

    public AvatarServerChain(Session session) {
        super(session);
    }

    @Override
    public IMessage handle(RawMessage raw) throws Exception {
        AvatarRequest request = new AvatarRequest(raw);

        if (session.member == null) throw new UnauthorizedException();

        AvatarRequest.Type type = request.getType();

        return switch (type) {
            case SET -> set(session.member);
            case GET_MY -> getMy(session.member);
            case GET_OF -> getOf(request.getNickname());
        };
    }

    private UUIDMessage set(MemberEntity you) throws Exception {
        JPAUtil jpaUtil = session.server.container.get(JPAUtil.class);

        FileMessage fileMessage = new FileMessage(queue.take());

        FileDTO dto = fileMessage.dto();

        if (!dto.contentType().startsWith("image/")) {
            throw new InvalidArgumentException();
        }

        FileEntity avatar = dbFileUtil.saveFile(dto, you.id().toString());

        if (!memberRepo.setAvatar(you, avatar)) {
            throw new ServerSandnodeErrorException();
        }

        session.member = jpaUtil.refresh(you);

        return new UUIDMessage(new Headers().setType(MessageTypes.HAPPY), avatar);
    }

    private FileMessage getMy(MemberEntity you) throws Exception {
        FileEntity avatar = you.avatar();
        if (avatar == null) throw new NoEffectException();
        return new FileMessage(dbFileUtil.readImage(avatar));
    }

    private FileMessage getOf(String nickname) throws Exception {
        MemberEntity member = memberRepo.findByNickname(nickname).orElseThrow(NotFoundException::new);
        FileEntity avatar = member.avatar();
        if (avatar == null) throw new NoEffectException();
        return new FileMessage(dbFileUtil.readImage(avatar));
    }
}
