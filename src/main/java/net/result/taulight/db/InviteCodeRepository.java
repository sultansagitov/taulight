package net.result.taulight.db;

import net.result.sandnode.db.MemberEntity;
import net.result.sandnode.exception.DatabaseException;
import net.result.taulight.exception.AlreadyExistingRecordException;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

public class InviteCodeRepository {
    public void save(InviteCodeEntity code) throws DatabaseException, AlreadyExistingRecordException {

    }

    public Optional<InviteCodeEntity> findByCode(String code) throws DatabaseException {
        return null;
    }

    public void delete(@NotNull InviteCodeEntity inviteCodeEntity) throws DatabaseException {

    }

    public void activate(InviteCodeEntity code) throws DatabaseException {
        code.setActivationDateNow();
        //
    }

    public List<InviteCodeEntity> findBySender(MemberEntity sender, boolean includeExpired, boolean includeActivated) throws DatabaseException {
        return null;
    }
}
