package net.result.sandnode.group;

import org.jetbrains.annotations.NotNull;

public class HashSetClientGroup extends HashSetGroup implements ClientGroup {
    private final String name;

    public HashSetClientGroup(@NotNull String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }
}
