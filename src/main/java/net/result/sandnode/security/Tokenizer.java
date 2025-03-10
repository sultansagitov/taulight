package net.result.sandnode.security;

import net.result.sandnode.exception.DatabaseException;
import net.result.sandnode.exception.error.ExpiredTokenException;
import net.result.sandnode.exception.error.InvalidTokenException;
import net.result.sandnode.db.Database;
import net.result.sandnode.db.Member;

import java.util.Optional;

public interface Tokenizer {
    String tokenizeMember(Member member);
    Optional<Member> findMember(Database database, String token)
            throws InvalidTokenException, ExpiredTokenException, DatabaseException;
}
