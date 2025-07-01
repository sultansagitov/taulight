package net.result.taulight.hubagent;

import net.result.sandnode.chain.ServerChainManager;
import net.result.sandnode.config.AgentConfig;
import net.result.sandnode.encryption.KeyStorageRegistry;
import net.result.sandnode.hubagent.Agent;
import net.result.taulight.chain.TauAgentServerChainManager;
import org.jetbrains.annotations.NotNull;

public class TauAgent extends Agent {
    public TauAgent(AgentConfig config) {
        this(new KeyStorageRegistry(), config);
    }

    public TauAgent(@NotNull KeyStorageRegistry keyStorageRegistry, AgentConfig config) {
        super(keyStorageRegistry, config);
    }

    @Override
    protected @NotNull ServerChainManager createChainManager() {
        return new TauAgentServerChainManager();
    }
}
