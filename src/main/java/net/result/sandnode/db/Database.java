package net.result.sandnode.db;

import net.result.sandnode.exception.error.BusyNicknameException;
import net.result.sandnode.exception.DatabaseException;
import net.result.sandnode.security.PasswordHasher;

import java.util.Optional;

/**
 * Interface representing a database layer.
 */
public interface Database {

    /**
     * Registers a new member with the given nickname and password.
     *
     * @param nickname the nickname of the member to register
     * @param password the raw password of the member to register
     * @return the registered {@link MemberEntity}
     * @throws BusyNicknameException if the nickname is already taken
     * @throws DatabaseException if a general database error occurs
     */
    MemberEntity registerMember(String nickname, String password) throws BusyNicknameException, DatabaseException;

    /**
     * Finds a member by their nickname.
     *
     * @param nickname the nickname to search for
     * @return an {@link Optional} containing the {@link MemberEntity} if found, or empty if not found
     * @throws DatabaseException if a database access error occurs
     */
    Optional<MemberEntity> findMemberByNickname(String nickname) throws DatabaseException;

    boolean deleteMember(MemberEntity member) throws DatabaseException;

    /**
     * Returns the {@link PasswordHasher} used for password hashing and verification.
     *
     * @return the {@link PasswordHasher} instance
     */
    PasswordHasher hasher();

}
