package net.result.sandnode.group;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;

public class HashSetGroupManager implements GroupManager {
    private final Collection<Group> groups;

    public HashSetGroupManager() {
        groups = new HashSet<>();
    }

    private @NotNull ClientGroup getClientGroup(@NotNull String groupName) {
        return new HashSetClientGroup(groupName);
    }

    @Override
    public Group getGroup(@NotNull String groupName) {
        Group group;

        Optional<Group> opt = getGroupOptional(groupName);
        if (opt.isPresent()) return opt.get();

        group = getClientGroup(groupName);
        groups.add(group);
        return group;
    }

    @Override
    public Optional<Group> getGroupOptional(@NotNull String groupName) {
        for (Group group : groups) {
            if (group instanceof ClientGroup clientGroup && clientGroup.getName().equals(groupName)) {
                return Optional.of(group);
            }
        }
        return Optional.empty();
    }
}
