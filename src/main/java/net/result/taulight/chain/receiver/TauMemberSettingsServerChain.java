package net.result.taulight.chain.receiver;

import net.result.sandnode.chain.ReceiverChain;
import net.result.sandnode.chain.ServerChain;
import net.result.sandnode.db.MemberUpdater;
import net.result.sandnode.exception.error.UnauthorizedException;
import net.result.sandnode.message.RawMessage;
import net.result.taulight.dto.TauMemberSettingsDTO;
import net.result.taulight.entity.TauMemberEntity;
import net.result.taulight.message.types.TauMemberSettingsRequest;
import net.result.taulight.message.types.TauMemberSettingsResponse;
import net.result.taulight.repository.TauMemberRepository;

public class TauMemberSettingsServerChain extends ServerChain implements ReceiverChain {
    @Override
    public TauMemberSettingsResponse handle(RawMessage raw) {
        if (session.member == null) throw new UnauthorizedException();

        TauMemberRepository repo = session.server.container.get(TauMemberRepository.class);
        MemberUpdater memberUpdater = session.server.container.get(MemberUpdater.class);

        TauMemberSettingsRequest request = new TauMemberSettingsRequest(raw);

        TauMemberEntity entity = session.member.getTauMember();

        Boolean showStatus = request.getShowStatus();
        if (showStatus != null) {
            repo.setShowStatus(entity, showStatus);
            memberUpdater.update(session);
        }

        TauMemberSettingsDTO dto = entity.toSettingsDTO();

        return new TauMemberSettingsResponse(dto);
    }
}
