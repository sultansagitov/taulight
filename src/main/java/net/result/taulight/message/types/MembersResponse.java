package net.result.taulight.message.types;

import com.fasterxml.jackson.core.type.TypeReference;
import net.result.sandnode.exception.DeserializationException;
import net.result.sandnode.exception.ExpectedMessageException;
import net.result.sandnode.message.MSGPackMessage;
import net.result.sandnode.message.RawMessage;
import net.result.sandnode.message.util.Headers;
import net.result.taulight.dto.MemberRecord;
import net.result.taulight.message.TauMessageTypes;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public class MembersResponse extends MSGPackMessage<Collection<MemberRecord>> {
    public MembersResponse(@NotNull Headers headers, @NotNull Collection<MemberRecord> records) {
        super(headers.setType(TauMessageTypes.MEMBERS), records);
    }

    public MembersResponse(@NotNull Collection<MemberRecord> records) {
        this(new Headers(), records);
    }

    public MembersResponse(@NotNull RawMessage message) throws DeserializationException, ExpectedMessageException {
        super(message.expect(TauMessageTypes.MEMBERS), new TypeReference<>() {});
    }

    public Collection<MemberRecord> getMembers() {
        return object;
    }
}
