package net.result.taulight.db;

import net.result.sandnode.db.MemberCreationListener;
import net.result.sandnode.entity.MemberEntity;
import net.result.sandnode.util.Container;
import net.result.taulight.repository.TauMemberRepository;

public class TauMemberCreationListener implements MemberCreationListener {
    private final TauMemberRepository tauMemberRepo;

    public TauMemberCreationListener(Container container) {
        this.tauMemberRepo = container.get(TauMemberRepository.class);
    }

    @Override
    public void onMemberCreated(MemberEntity member) {
        tauMemberRepo.create(member);
    }
}
