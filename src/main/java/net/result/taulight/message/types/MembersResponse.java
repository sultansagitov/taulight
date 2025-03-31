package net.result.taulight.message.types;

import com.fasterxml.jackson.core.type.TypeReference;
import net.result.sandnode.db.Member;
import net.result.sandnode.exception.DeserializationException;
import net.result.sandnode.exception.ExpectedMessageException;
import net.result.sandnode.message.MSGPackMessage;
import net.result.sandnode.message.RawMessage;
import net.result.sandnode.message.util.Headers;
import net.result.taulight.message.MemberRecord;
import net.result.taulight.message.TauMessageTypes;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.stream.Collectors;

public class MembersResponse extends MSGPackMessage<Collection<MemberRecord>> {
    public MembersResponse(@NotNull Headers headers, @NotNull Collection<Member> members) {
        super(headers.setType(TauMessageTypes.MEMBERS), members.stream()
                .map(MemberRecord::new)
                .collect(Collectors.toSet()));
    }

    public MembersResponse(Collection<Member> members) {
        this(new Headers(), members);
    }

    public MembersResponse(@NotNull RawMessage message) throws DeserializationException, ExpectedMessageException {
        super(message.expect(TauMessageTypes.MEMBERS), new TypeReference<>() {});
    }

    public Collection<MemberRecord> getMembers() {
        return object;
    }
}
