package net.result.sandnode.util.db;

import java.util.Optional;

public interface IDatabase {

    IMember registerMember(String agentID, String password);

    Optional<IMember> findMemberByMemberID(String agentID);

}
