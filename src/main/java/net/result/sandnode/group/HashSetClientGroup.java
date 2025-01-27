package net.result.sandnode.group;

import org.jetbrains.annotations.NotNull;

public class HashSetClientGroup extends HashSetGroup implements ClientGroup {
    public HashSetClientGroup(@NotNull String id) {
        super(id);
    }

    @Override
    public String getID() {
        return "#%s".formatted(super.getID());
    }
}
