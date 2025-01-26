package net.result.sandnode.message.types;

import net.result.sandnode.exception.ExpectedMessageException;
import net.result.sandnode.message.IMessage;
import net.result.sandnode.message.Message;
import net.result.sandnode.message.util.Headers;
import net.result.sandnode.message.util.MessageTypes;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class GroupMessage extends Message {
    private static final Logger LOGGER = LogManager.getLogger(GroupMessage.class);

    @Unmodifiable
    private final Collection<String> groupNames;

    public GroupMessage(@NotNull Headers headers, @NotNull Collection<String> groupNames) {
        super(headers.setType(MessageTypes.GROUP));
        Set<String> filteredGroupNames = new HashSet<>();
        for (String s : groupNames) {
            if (s == null || s.trim().isEmpty()) {
                LOGGER.warn("Skipping invalid group name: null or empty");
                continue;
            }

            String str = s.trim().toLowerCase();

            if (!str.matches("^#?[a-z0-9_]+$")) {
                LOGGER.warn("Skipping invalid group name: {}", s);
                continue;
            }

            filteredGroupNames.add(str);
        }
        this.groupNames = filteredGroupNames;
    }

    public GroupMessage(@NotNull Collection<String> groupNames) {
        this(new Headers(), groupNames);
    }

    public GroupMessage(@NotNull IMessage message) throws ExpectedMessageException {
        super(message.expect(MessageTypes.GROUP).getHeaders());

        String[] groupNames = new String(message.getBody()).split(",");
        this.groupNames = Arrays
                .stream(groupNames)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(String::toLowerCase)
                .filter(str -> str.matches("^#?[a-z0-9_]+$"))
                .map(str -> str.startsWith("#") ? str : "#" + str)
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
