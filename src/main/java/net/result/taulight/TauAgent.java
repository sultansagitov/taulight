package net.result.taulight;

import net.result.sandnode.Agent;
import net.result.sandnode.config.IAgentConfig;
import net.result.sandnode.messages.IMessage;
import net.result.sandnode.server.Session;
import net.result.sandnode.encryption.GlobalKeyStorage;
import org.jetbrains.annotations.NotNull;

public class TauAgent extends Agent {

    public TauAgent(@NotNull IAgentConfig agentConfig) {
        this(new GlobalKeyStorage(), agentConfig);
    }

    public TauAgent(@NotNull GlobalKeyStorage globalKeyStorage, @NotNull IAgentConfig agentConfig) {
        super(globalKeyStorage, agentConfig);
    }

    @Override
    public void onAgentMessage(
            @NotNull IMessage request,
            @NotNull Session session
    ) {
    }
}
