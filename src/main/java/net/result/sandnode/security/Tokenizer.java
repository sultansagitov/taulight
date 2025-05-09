package net.result.sandnode.security;

import net.result.sandnode.exception.DatabaseException;
import net.result.sandnode.exception.error.ExpiredTokenException;
import net.result.sandnode.exception.error.InvalidTokenException;
import net.result.sandnode.db.MemberEntity;

import java.util.Optional;

public interface Tokenizer {
    String tokenizeMember(MemberEntity member);
    Optional<MemberEntity> findMember(String token)
            throws InvalidTokenException, ExpiredTokenException, DatabaseException;
}
