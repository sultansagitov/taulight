package net.result.sandnode.tokens;

import net.result.sandnode.exception.ExpiredTokenException;
import net.result.sandnode.exception.InvalidTokenException;
import net.result.sandnode.db.Database;
import net.result.sandnode.db.Member;

import java.util.Optional;

public interface Tokenizer {
    String tokenizeMember(Member member);
    Optional<Member> findMember(Database database, String token) throws InvalidTokenException, ExpiredTokenException;
}
