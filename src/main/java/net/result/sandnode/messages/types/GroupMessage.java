package net.result.sandnode.messages.types;

import net.result.sandnode.exceptions.ExpectedMessageException;
import net.result.sandnode.messages.IMessage;
import net.result.sandnode.messages.Message;
import net.result.sandnode.messages.util.Headers;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.HashSet;
import java.util.Set;

import static net.result.sandnode.messages.util.MessageTypes.GROUP;

public class GroupMessage extends Message {

    @Unmodifiable
    private final Set<String> groupNames;

    public GroupMessage(@NotNull Headers headers, @NotNull Set<String> groupNames) {
        super(headers.set(GROUP));
        this.groupNames = new HashSet<>(groupNames);
    }

    public GroupMessage(@NotNull Headers headers, @NotNull String groupName) {
        this(headers.set(GROUP), Set.of(groupName));
    }

    public GroupMessage(@NotNull IMessage message) throws ExpectedMessageException {
        super(message.getHeaders());
        ExpectedMessageException.check(message, GROUP);

        String s = new String(message.getBody());
        String[] split = s.split(";");
        groupNames = Set.of(split);
    }

    @Override
    public byte[] getBody() {
        return String.join(";", getGroupNames()).getBytes();
    }

    public Set<String> getGroupNames() {
        return groupNames;
    }
}
