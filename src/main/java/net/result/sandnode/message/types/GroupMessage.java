package net.result.sandnode.message.types;

import net.result.sandnode.exception.ExpectedMessageException;
import net.result.sandnode.message.IMessage;
import net.result.sandnode.message.Message;
import net.result.sandnode.message.util.Headers;
import net.result.sandnode.message.util.MessageTypes;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.stream.Collectors;

public class GroupMessage extends Message {
    @Unmodifiable
    private final Collection<String> groupNames;

    public GroupMessage(@NotNull Collection<String> groupNames) {
        this(new Headers(), groupNames);
    }

    public GroupMessage(@NotNull Headers headers, @NotNull Collection<String> groupNames) {
        super(headers.setType(MessageTypes.GROUP));
        this.groupNames = new HashSet<>(groupNames);
    }

    public GroupMessage(@NotNull IMessage message) throws ExpectedMessageException {
        super(message.expect(MessageTypes.GROUP).getHeaders());

        String[] groupNames = new String(message.getBody()).split(",");
        this.groupNames = Arrays
                .stream(groupNames)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toSet());
    }

    public Collection<String> getGroupNames() {
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
