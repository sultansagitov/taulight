package net.result.sandnode.util.group;

import net.result.sandnode.server.Session;
import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.Set;

public class GroupManager implements IGroupManager {
    private final Set<IGroup> groups;

    public GroupManager() {
        groups = new HashSet<>();
    }

    @Override
    public synchronized void addToGroup(@NotNull String groupName, @NotNull Session session) {
        for (IGroup group : groups) {
            if (group instanceof ClientGroup && ((ClientGroup) group).id.equals(groupName)) {
                group.add(session);
                return;
            }
        }

        IGroup group = new ClientGroup(groupName);
        group.add(session);
        groups.add(group);
    }

    @Override
    public synchronized void addToGroup(@NotNull Set<String> groupNames, @NotNull Session session) {
        for (String groupName : groupNames) {
            addToGroup(groupName, session);
        }
    }

    @Override
    public synchronized Set<Session> getSessions(@NotNull String groupName) {
        for (IGroup group : groups) {
            if (group instanceof ClientGroup && ((ClientGroup) group).id.equals(groupName)) {
                return group.getSessions();
            }
        }

        return new HashSet<>();
    }

    @Override
    public @NotNull Set<String> getGroups(Session session) {
        Set<String> groupNames = new HashSet<>();

        for (IGroup group : groups) {
            if (group instanceof ClientGroup && group.getSessions().contains(session)) {
                groupNames.add(((ClientGroup) group).id);
            }
        }

        return groupNames;
    }



}
