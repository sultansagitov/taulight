package net.result.taulight.group;

import net.result.sandnode.group.HashSetGroupManager;
import net.result.taulight.db.ChatEntity;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class HashSetTauGroupManager extends HashSetGroupManager implements TauGroupManager {
    public final Map<UUID, ChatGroup> map = new HashMap<>();

    @Override
    public ChatGroup getGroup(@NotNull ChatEntity chat) {
        UUID id = chat.id();

        if (!map.containsKey(id)) {
            ChatGroup group = new ChatGroup(id);
            add(group);
            map.put(id, group);
            return group;
        }

        return map.get(id);
    }
}
