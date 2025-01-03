package net.result.sandnode.util.group;

import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class HashSetGroupManager implements GroupManager {
    private final Set<Group> groups;

    public HashSetGroupManager() {
        groups = new HashSet<>();
    }

    @Override
    public Group getGroup(@NotNull String groupName) {
        Group group;

        Optional<Group> opt = getGroupOptional(groupName);
        if (opt.isPresent()) return opt.get();

        group = new ClientNamedGroup(groupName);
        groups.add(group);
        return group;
    }

    @Override
    public Optional<Group> getGroupOptional(@NotNull String groupName) {
        for (Group group : groups) {
            if (group instanceof ClientNamedGroup && ((ClientNamedGroup) group).name.equals(groupName)) {
                return Optional.of(group);
            }
        }
        return Optional.empty();
    }
}
