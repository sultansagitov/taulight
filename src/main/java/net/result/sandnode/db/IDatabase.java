package net.result.sandnode.db;

import net.result.sandnode.exception.BusyMemberIDException;

import java.util.Optional;

public interface IDatabase {

    IMember registerMember(String agentID, String password) throws BusyMemberIDException;

    Optional<IMember> findMemberByMemberID(String agentID);

}
