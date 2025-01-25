package net.result.sandnode.message.types;

import net.result.sandnode.exception.ExpectedMessageException;
import net.result.sandnode.message.IMessage;
import net.result.sandnode.message.Message;
import net.result.sandnode.message.util.Headers;
import net.result.sandnode.message.util.MessageTypes;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class GroupMessage extends Message {
    @Unmodifiable
    private final Set<String> groupNames;

    public GroupMessage(@NotNull Set<String> groupNames) {
        this(new Headers(), groupNames);
    }

    public GroupMessage(@NotNull Headers headers, @NotNull Set<String> groupNames) {
        super(headers.setType(MessageTypes.GROUP));
        this.groupNames = new HashSet<>(groupNames);
    }

    public GroupMessage(@NotNull IMessage message) throws ExpectedMessageException {
        super(message.getHeaders());
        ExpectedMessageException.check(message, MessageTypes.GROUP);

        String[] groupNames = new String(message.getBody()).split(",");
        this.groupNames = Arrays
                .stream(groupNames)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toSet());
    }

    public Set<String> getGroupNames() {
        return groupNames;
    }

    public String toString() {
        return super.toString() + " " + groupNames;
    }

    @Override
    public byte[] getBody() {
        return String.join(",", groupNames).getBytes();
    }
}
