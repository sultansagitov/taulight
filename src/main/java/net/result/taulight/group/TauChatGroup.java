package net.result.taulight.group;

import net.result.sandnode.group.HashSetGroup;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class TauChatGroup extends HashSetGroup {
    public TauChatGroup(@NotNull UUID chatID) {
        super(chatID.toString());
    }

    @Override
    public String getID() {
        return "@%s".formatted(super.getID());
    }
}
