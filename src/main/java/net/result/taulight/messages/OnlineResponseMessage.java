package net.result.taulight.messages;

import com.fasterxml.jackson.annotation.JsonProperty;
import net.result.sandnode.exceptions.DeserializationException;
import net.result.sandnode.exceptions.ExpectedMessageException;
import net.result.sandnode.messages.IMessage;
import net.result.sandnode.messages.MSGPackMessage;
import net.result.sandnode.messages.util.Headers;
import net.result.sandnode.util.db.IMember;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import static net.result.taulight.messages.TauMessageTypes.ONL;

public class OnlineResponseMessage extends MSGPackMessage<OnlineResponseMessage.MemberSetData> {
    public static class MemberSetData {
        @JsonProperty
        public Set<String> members;

        public MemberSetData() {}
        public MemberSetData(Set<String> members) {
            this.members = new HashSet<>(members);
        }
    }

    public OnlineResponseMessage(@NotNull Headers headers, @NotNull Set<IMember> members) {
        super(
                headers.setType(ONL),
                new MemberSetData(members.stream().map(IMember::getID).collect(Collectors.toSet()))
        );
    }

    public OnlineResponseMessage(@NotNull Set<IMember> members) {
        this(new Headers(), members);
    }

    public OnlineResponseMessage(@NotNull IMessage message) throws DeserializationException, ExpectedMessageException {
        super(message, MemberSetData.class);
        ExpectedMessageException.check(message, ONL);
    }

    public Set<String> getMembers() {
        return object.members;
    }
}