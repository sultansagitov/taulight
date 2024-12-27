package net.result.sandnode.util.group;

import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public interface IGroupManager {

    Optional<IGroup> getGroupOptional(@NotNull String groupName);

    IGroup getGroup(@NotNull String groupName);

}
