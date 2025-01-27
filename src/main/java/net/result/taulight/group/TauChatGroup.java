package net.result.taulight.group;

import net.result.sandnode.group.HashSetGroup;
import org.jetbrains.annotations.NotNull;

public class TauChatGroup extends HashSetGroup {
    public TauChatGroup(@NotNull String id) {
        super(id);
    }

    @Override
    public String getID() {
        return "@%s".formatted(super.getID());
    }
}
