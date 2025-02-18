package net.result.taulight.group;

import net.result.sandnode.group.HashSetGroupManager;
import net.result.taulight.db.TauChat;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class HashSetTauGroupManager extends HashSetGroupManager implements TauGroupManager {
    public final Map<UUID, TauChatGroup> map = new HashMap<>();

    @Override
    public TauChatGroup getGroup(@NotNull TauChat chat) {
        UUID id = chat.id();

        if (!map.containsKey(id)) {
            TauChatGroup group = new TauChatGroup(id);
            add(group);
            map.put(id, group);
            return group;
        }

        return map.get(id);
    }
}
