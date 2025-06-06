package net.result.sandnode.group;

import net.result.sandnode.serverclient.Session;
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
        return getGroupOptional(groupID).orElseGet(() -> {
            Group group = getClientGroup(groupID.substring(1));
            groups.add(group);
            return group;
        });
    }

    public void add(Group group) {
        groups.add(group);
    }

    private Optional<Group> getGroupOptional(@NotNull String groupID) {
        return groups.stream().filter(group -> group.getID().equals(groupID)).findFirst();
    }

    @Override
    public void removeSession(@NotNull Session session) {
        groups.forEach(session::removeFromGroup);
    }
}
