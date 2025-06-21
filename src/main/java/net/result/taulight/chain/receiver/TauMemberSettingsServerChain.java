package net.result.taulight.chain.receiver;

import net.result.sandnode.chain.ReceiverChain;
import net.result.sandnode.chain.ServerChain;
import net.result.sandnode.exception.error.UnauthorizedException;
import net.result.sandnode.message.util.Headers;
import net.result.sandnode.serverclient.Session;
import net.result.sandnode.util.JPAUtil;
import net.result.taulight.db.TauMemberEntity;
import net.result.taulight.db.TauMemberRepository;
import net.result.taulight.dto.TauMemberSettingsResponseDTO;
import net.result.taulight.message.types.TauMemberSettingsRequest;
import net.result.taulight.message.types.TauMemberSettingsResponse;

import java.util.Optional;

public class TauMemberSettingsServerChain extends ServerChain implements ReceiverChain {
    public TauMemberSettingsServerChain(Session session) {
        super(session);
    }

    @Override
    public void sync() throws Exception {
        TauMemberSettingsRequest request = new TauMemberSettingsRequest(queue.take());

        if (session.member == null) throw new UnauthorizedException();

        JPAUtil jpaUtil = session.server.container.get(JPAUtil.class);
        TauMemberRepository repo = session.server.container.get(TauMemberRepository.class);

        session.member = jpaUtil.refresh(session.member);

        Headers headers = request.headers();

        TauMemberEntity entity = session.member.tauMember();

        Optional<String> showStatus = headers.getOptionalValue(TauMemberSettingsRequest.SHOW_STATUS);
        if (showStatus.isPresent()) {
            repo.setShowStatus(entity, Boolean.parseBoolean(showStatus.get()));
        }

        TauMemberSettingsResponseDTO dto = new TauMemberSettingsResponseDTO(entity);

        sendFin(new TauMemberSettingsResponse(dto));
    }
}
