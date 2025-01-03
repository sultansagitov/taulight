package net.result.sandnode.util.group;

import org.jetbrains.annotations.NotNull;

public class ClientNamedGroup extends HashSetGroup {
    public final String name;

    public ClientNamedGroup(@NotNull String name) {
        this.name = name;
    }

}
