package net.result.sandnode.util.group;

import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public interface GroupManager {

    Optional<Group> getGroupOptional(@NotNull String groupName);

    Group getGroup(@NotNull String groupName);

}
