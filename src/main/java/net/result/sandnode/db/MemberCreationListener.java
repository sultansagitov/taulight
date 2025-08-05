package net.result.sandnode.db;

import net.result.sandnode.exception.DatabaseException;

public interface MemberCreationListener {
    void onMemberCreated(MemberEntity member) throws DatabaseException;
}
