package net.result.sandnode.messages.types;

import net.result.sandnode.exceptions.ExpectedMessageException;
import net.result.sandnode.messages.EmptyMessage;
import net.result.sandnode.messages.IMessage;
import net.result.sandnode.messages.util.Headers;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.HashSet;
import java.util.Set;

import static net.result.sandnode.messages.util.MessageType.GROUP;

public class GroupMessage extends EmptyMessage {
    @Unmodifiable
    private final Set<String> groupNames;

    public GroupMessage(@NotNull Set<String> groupNames) {
        this(new Headers(), groupNames);
    }

    public GroupMessage(@NotNull Headers headers, @NotNull Set<String> groupNames) {
        super(headers.setValue("groups", String.join(",", groupNames)).setType(GROUP));
        this.groupNames = new HashSet<>(groupNames);
    }

    public GroupMessage(@NotNull IMessage message) throws ExpectedMessageException {
        super(message.getHeaders());
        ExpectedMessageException.check(message, GROUP);

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
