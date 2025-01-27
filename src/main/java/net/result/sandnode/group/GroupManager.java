package net.result.sandnode.group;

import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public interface GroupManager {

    Optional<Group> getGroupOptional(@NotNull String groupID);

    Group getGroup(@NotNull String groupID);

    void add(Group group);
}
