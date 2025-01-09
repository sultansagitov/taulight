package net.result.sandnode.util.tokens;

import net.result.sandnode.exceptions.InvalidTokenException;
import net.result.sandnode.util.db.IDatabase;
import net.result.sandnode.util.db.IMember;

import java.util.Optional;

public interface ITokenizer {
    String tokenizeMember(IMember member);
    Optional<IMember> findMember(IDatabase database, String token) throws InvalidTokenException;
}
