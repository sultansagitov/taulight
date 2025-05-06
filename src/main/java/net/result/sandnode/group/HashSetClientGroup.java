package net.result.sandnode.group;

import org.jetbrains.annotations.NotNull;

public class HashSetClientGroup extends HashSetGroup implements ClientGroup {
    public HashSetClientGroup(@NotNull String id) {
        super("#%s".formatted(id));
    }
}
