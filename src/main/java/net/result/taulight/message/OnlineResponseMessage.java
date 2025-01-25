package net.result.taulight.message;

import com.fasterxml.jackson.annotation.JsonProperty;
import net.result.sandnode.exception.DeserializationException;
import net.result.sandnode.exception.ExpectedMessageException;
import net.result.sandnode.message.IMessage;
import net.result.sandnode.message.MSGPackMessage;
import net.result.sandnode.message.util.Headers;
import net.result.sandnode.db.IMember;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashSet;
import java.util.stream.Collectors;

import static net.result.taulight.message.TauMessageTypes.ONL;

public class OnlineResponseMessage extends MSGPackMessage<OnlineResponseMessage.MemberSetData> {
    public static class MemberSetData {
        @JsonProperty
        public Collection<String> members;

        public MemberSetData() {}
        public MemberSetData(Collection<String> members) {
            this.members = new HashSet<>(members);
        }
    }

    public OnlineResponseMessage(@NotNull Headers headers, @NotNull Collection<IMember> members) {
        super(
                headers.setType(ONL),
                new MemberSetData(members.stream().map(IMember::getID).collect(Collectors.toSet()))
        );
    }

    public OnlineResponseMessage(@NotNull Collection<IMember> members) {
        this(new Headers(), members);
    }

    public OnlineResponseMessage(@NotNull IMessage message) throws DeserializationException, ExpectedMessageException {
        super(message, MemberSetData.class);
        ExpectedMessageException.check(message, ONL);
    }

    public Collection<String> getMembers() {
        return object.members;
    }
}