package net.result.sandnode.util.db;

import net.result.sandnode.exceptions.BusyMemberIDException;

import java.util.Optional;

public interface IDatabase {

    IMember registerMember(String agentID, String password) throws BusyMemberIDException;

    Optional<IMember> findMemberByMemberID(String agentID);

}
