package net.result.sandnode.message.types;

import net.result.sandnode.exception.ExpectedMessageException;
import net.result.sandnode.message.RawMessage;
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

public class GroupRequest extends Message {
    private static final Logger LOGGER = LogManager.getLogger(GroupRequest.class);

    @Unmodifiable
    private final Collection<String> groupsID;

    public GroupRequest(@NotNull Headers headers, @NotNull Collection<String> groupsID) {
        super(headers.setType(MessageTypes.GROUP));
        Set<String> filteredGroupNames = new HashSet<>();
        for (String s : groupsID) {
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
        this.groupsID = filteredGroupNames;
    }

    public GroupRequest(@NotNull Collection<String> groupsID) {
        this(new Headers(), groupsID);
    }

    public GroupRequest(@NotNull RawMessage message) throws ExpectedMessageException {
        super(message.expect(MessageTypes.GROUP).headers());

        String[] groupsID = new String(message.getBody()).split(",");
        this.groupsID = Arrays
                .stream(groupsID)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(String::toLowerCase)
                .filter(str -> str.matches("^#?[a-z0-9_]+$"))
                .map(str -> str.startsWith("#") ? str : "#" + str)
                .collect(Collectors.toSet());

    }

    public Collection<String> getGroupsID() {
        return groupsID;
    }

    public String toString() {
        return super.toString() + " " + groupsID;
    }

    @Override
    public byte[] getBody() {
        return String.join(",", groupsID).getBytes();
    }
}
