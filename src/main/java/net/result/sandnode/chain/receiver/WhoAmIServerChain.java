package net.result.sandnode.chain.receiver;

import net.result.sandnode.chain.ReceiverChain;
import net.result.sandnode.chain.ServerChain;
import net.result.sandnode.db.FileEntity;
import net.result.sandnode.db.MemberEntity;
import net.result.sandnode.db.MemberRepository;
import net.result.sandnode.dto.FileDTO;
import net.result.sandnode.exception.error.NoEffectException;
import net.result.sandnode.exception.error.ServerSandnodeErrorException;
import net.result.sandnode.exception.error.UnauthorizedException;
import net.result.sandnode.message.types.FileMessage;
import net.result.sandnode.message.types.HappyMessage;
import net.result.sandnode.message.types.WhoAmIRequest;
import net.result.sandnode.message.types.WhoAmIResponse;
import net.result.sandnode.serverclient.Session;
import net.result.sandnode.util.DBFileUtil;
import net.result.sandnode.util.JPAUtil;

public class WhoAmIServerChain extends ServerChain implements ReceiverChain {
    private final DBFileUtil dbFileUtil = session.server.container.get(DBFileUtil.class);
    private final MemberRepository memberRepo = session.server.container.get(MemberRepository.class);

    public WhoAmIServerChain(Session session) {
        super(session);
    }

    @Override
    public void sync() throws Exception {
        WhoAmIRequest request = new WhoAmIRequest(queue.take());

        if (session.member == null) throw new UnauthorizedException();

        WhoAmIRequest.Type type = request.getType();

        sendFin(switch (type) {
            case NICKNAME -> nickname(session.member);
            case GET_AVATAR -> getAvatar(session.member);
            case SET_AVATAR -> setAvatar(session.member);
        });
    }

    private WhoAmIResponse nickname(MemberEntity you) {
        return new WhoAmIResponse(you);
    }

    private FileMessage getAvatar(MemberEntity you) throws Exception {
        FileEntity avatar = you.avatar();
        if (avatar == null) throw new NoEffectException();
        return new FileMessage(dbFileUtil.readImage(avatar));
    }

    private HappyMessage setAvatar(MemberEntity you) throws Exception {
        JPAUtil jpaUtil = session.server.container.get(JPAUtil.class);

        FileMessage fileMessage = new FileMessage(queue.take());

        FileDTO dto = fileMessage.dto();
        FileEntity avatar = dbFileUtil.saveImage(dto, you.id().toString());

        if (!memberRepo.setAvatar(you, avatar)) {
            throw new ServerSandnodeErrorException();
        }

        session.member = jpaUtil.refresh(you);

        return new HappyMessage();
    }
}
