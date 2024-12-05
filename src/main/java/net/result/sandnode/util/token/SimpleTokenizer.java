package net.result.sandnode.util.token;

import net.result.sandnode.util.db.IDatabase;
import net.result.sandnode.util.db.IMember;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class SimpleTokenizer implements ITokenizer {
    @Override
    public String tokenizeMember(@NotNull IMember member) {
        return String.format("%s@%s", member.getID(), member.getHashedPassword());
    }

    @Override
    public Optional<IMember> findMember(@NotNull IDatabase database, @NotNull String token) {
        return database.findMemberByMemberID(token.split("@")[0]);
    }
}
