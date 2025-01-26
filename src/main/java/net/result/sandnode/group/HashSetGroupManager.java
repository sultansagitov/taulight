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

    private @NotNull ClientGroup getClientGroup(@NotNull String clearGroupName) {
        return new HashSetClientGroup(clearGroupName);
    }

    @Override
    public Group getGroup(@NotNull String groupID) {
        Group group;

        Optional<Group> opt = getGroupOptional(groupID);
        if (opt.isPresent()) return opt.get();

        group = getClientGroup(groupID.substring(1));
        groups.add(group);
        return group;
    }

    @Override
    public Optional<Group> getGroupOptional(@NotNull String groupID) {
        for (Group group : groups) {
            if (group.getID().equals(groupID)) {
                return Optional.of(group);
            }
        }
        return Optional.empty();
    }
}
