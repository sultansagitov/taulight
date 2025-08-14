package net.result.taulight.cluster;

import net.result.sandnode.cluster.HashSetClusterManager;
import net.result.taulight.entity.ChatEntity;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class HashSetTauClusterManager extends HashSetClusterManager implements TauClusterManager {
    public final Map<UUID, ChatCluster> map = new HashMap<>();

    @Override
    public ChatCluster getCluster(@NotNull ChatEntity chat) {
        UUID id = chat.id();

        if (!map.containsKey(id)) {
            ChatCluster cluster = new ChatCluster(id);
            add(cluster);
            map.put(id, cluster);
            return cluster;
        }

        return map.get(id);
    }
}
