package net.result.sandnode.db;

import net.result.sandnode.entity.MemberEntity;

public interface MemberCreationListener {
    void onMemberCreated(MemberEntity member);
}
