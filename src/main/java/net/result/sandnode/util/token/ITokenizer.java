package net.result.sandnode.util.token;

import net.result.sandnode.util.db.IDatabase;
import net.result.sandnode.util.db.IMember;

import java.util.Optional;

public interface ITokenizer {
    String tokenizeMember(IMember member);
    Optional<IMember> findMember(IDatabase database, String token);
}
