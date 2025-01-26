package net.result.sandnode.tokens;

import net.result.sandnode.exception.InvalidTokenException;
import net.result.sandnode.db.IDatabase;
import net.result.sandnode.db.Member;

import java.util.Optional;

public interface ITokenizer {
    String tokenizeMember(Member member);
    Optional<Member> findMember(IDatabase database, String token) throws InvalidTokenException;
}
