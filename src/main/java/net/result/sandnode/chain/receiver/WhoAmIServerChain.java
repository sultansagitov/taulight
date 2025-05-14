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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class WhoAmIServerChain extends ServerChain implements ReceiverChain {

    private static final Logger LOGGER = LogManager.getLogger(WhoAmIServerChain.class);
    private final DBFileUtil dbFileUtil = session.server.container.get(DBFileUtil.class);
    private final MemberRepository memberRepo = session.server.container.get(MemberRepository.class);

    public WhoAmIServerChain(Session session) {
        super(session);
    }

    @Override
    public void sync() throws Exception {
        WhoAmIRequest request = new WhoAmIRequest(queue.take());

        MemberEntity member = session.member;

        if (member == null) {
            throw new UnauthorizedException();
        }

        WhoAmIRequest.Type type = request.getType();

        LOGGER.debug(type);

        sendFin(switch (type) {
            case NICKNAME -> new WhoAmIResponse(member);
            case GET_AVATAR -> {
                FileEntity avatar = member.avatar();
                if (avatar == null) throw new NoEffectException();
                yield new FileMessage(dbFileUtil.readImage(avatar));
            }
            case SET_AVATAR -> {
                FileMessage fileMessage = new FileMessage(queue.take());

                FileDTO dto = fileMessage.dto();
                FileEntity avatar = dbFileUtil.saveImage(dto, member.id().toString());

                if (!memberRepo.setAvatar(member, avatar)) {
                    throw new ServerSandnodeErrorException();
                }

                yield new HappyMessage();
            }
        });
    }
}
