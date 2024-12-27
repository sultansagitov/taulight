package net.result.sandnode.util.group;

import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class GroupManager implements IGroupManager {
    private final Set<IGroup> groups;

    public GroupManager() {
        groups = new HashSet<>();
    }

    @Override
    public IGroup getGroup(@NotNull String groupName) {
        IGroup group;

        Optional<IGroup> opt = getGroupOptional(groupName);
        if (opt.isPresent()) return opt.get();

        group = new ClientGroup(groupName);
        groups.add(group);
        return group;
    }

    @Override
    public Optional<IGroup> getGroupOptional(@NotNull String groupName) {
        for (IGroup group : groups) {
            if (group instanceof ClientGroup && ((ClientGroup) group).name.equals(groupName)) {
                return Optional.of(group);
            }
        }
        return Optional.empty();
    }
}
