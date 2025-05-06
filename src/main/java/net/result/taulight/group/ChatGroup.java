package net.result.taulight.group;

import net.result.sandnode.group.HashSetGroup;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class ChatGroup extends HashSetGroup {
    public ChatGroup(@NotNull UUID chatID) {
        super("@%s".formatted(chatID));
    }
}
