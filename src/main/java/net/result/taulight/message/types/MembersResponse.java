package net.result.taulight.message.types;

import com.fasterxml.jackson.annotation.JsonProperty;
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

public class MembersResponse extends MSGPackMessage<MembersResponse.Data> {
    protected static class Data {
        @JsonProperty
        private Collection<MemberRecord> members;

        @SuppressWarnings("unused")
        public Data() {}
        public Data(@NotNull Collection<Member> members) {
            this.members = members.stream().map(MemberRecord::new).collect(Collectors.toSet());
        }
    }

    public MembersResponse(@NotNull Headers headers, Collection<Member> members) {
        super(headers.setType(TauMessageTypes.MEMBERS), new Data(members));
    }

    public MembersResponse(Collection<Member> members) {
        this(new Headers(), members);
    }

    public MembersResponse(@NotNull RawMessage message) throws DeserializationException, ExpectedMessageException {
        super(message.expect(TauMessageTypes.MEMBERS), Data.class);
    }

    public Collection<MemberRecord> getMembers() {
        return object.members;
    }
}
