package net.result.sandnode.message.types;

import net.result.sandnode.exception.ExpectedMessageException;
import net.result.sandnode.message.EmptyMessage;
import net.result.sandnode.message.IMessage;
import net.result.sandnode.message.util.Headers;
import net.result.sandnode.message.util.MessageTypes;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.HashSet;
import java.util.Set;

public class GroupMessage extends EmptyMessage {
    @Unmodifiable
    private final Set<String> groupNames;

    public GroupMessage(@NotNull Set<String> groupNames) {
        this(new Headers(), groupNames);
    }

    public GroupMessage(@NotNull Headers headers, @NotNull Set<String> groupNames) {
        super(headers.setValue("groups", String.join(",", groupNames)).setType(MessageTypes.GROUP));
        this.groupNames = new HashSet<>(groupNames);
    }

    public GroupMessage(@NotNull IMessage message) throws ExpectedMessageException {
        super(message.getHeaders());
        ExpectedMessageException.check(message, MessageTypes.GROUP);

        String s = message.getHeaders().getValue("groups");
        groupNames = Set.of(s.split(","));
    }

    public Set<String> getGroupNames() {
        return groupNames;
    }

    public String toString() {
        return super.toString() + " " + groupNames;
    }
}
