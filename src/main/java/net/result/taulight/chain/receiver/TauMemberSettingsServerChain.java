package net.result.taulight.chain.receiver;

import net.result.sandnode.chain.ReceiverChain;
import net.result.sandnode.chain.ServerChain;
import net.result.sandnode.exception.error.UnauthorizedException;
import net.result.sandnode.message.RawMessage;
import net.result.sandnode.message.util.Headers;
import net.result.sandnode.serverclient.Session;
import net.result.sandnode.db.JPAUtil;
import net.result.taulight.entity.TauMemberEntity;
import net.result.taulight.repository.TauMemberRepository;
import net.result.taulight.dto.TauMemberSettingsDTO;
import net.result.taulight.message.types.TauMemberSettingsRequest;
import net.result.taulight.message.types.TauMemberSettingsResponse;

import java.util.Optional;

public class TauMemberSettingsServerChain extends ServerChain implements ReceiverChain {
    public TauMemberSettingsServerChain(Session session) {
        super(session);
    }

    @Override
    public TauMemberSettingsResponse handle(RawMessage raw) throws Exception {
        TauMemberSettingsRequest request = new TauMemberSettingsRequest(raw);

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

        TauMemberSettingsDTO dto = entity.toSettingsDTO();

        return new TauMemberSettingsResponse(dto);
    }
}
