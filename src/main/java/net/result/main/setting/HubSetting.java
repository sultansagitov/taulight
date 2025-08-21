package net.result.main.setting;

import net.result.main.config.HubPropertiesConfig;
import net.result.main.config.JWTPropertiesConfig;
import net.result.main.config.ServerPropertiesConfig;
import net.result.sandnode.db.SimpleJPAUtil;
import net.result.sandnode.security.JWTTokenizer;
import net.result.sandnode.util.Container;
import net.result.taulight.cluster.HashSetTauClusterManager;
import net.result.taulight.db.TauMemberCreationListener;

public class HubSetting {
    public HubSetting(Container container) {
        container.set(SimpleJPAUtil.class);

        container.set(HubPropertiesConfig.class);
        container.set(ServerPropertiesConfig.class);

        container.set(HashSetTauClusterManager.class);

        container.set(JWTPropertiesConfig.class);
        container.set(JWTTokenizer.class);

        container.addInstanceItem(TauMemberCreationListener.class);
    }
}
