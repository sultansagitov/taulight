package net.result.sandnode.tokens;

import net.result.sandnode.exception.InvalidTokenException;
import net.result.sandnode.db.IDatabase;
import net.result.sandnode.db.IMember;

import java.util.Optional;

public interface ITokenizer {
    String tokenizeMember(IMember member);
    Optional<IMember> findMember(IDatabase database, String token) throws InvalidTokenException;
}
