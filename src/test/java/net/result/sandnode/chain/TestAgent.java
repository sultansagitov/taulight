package net.result.sandnode.chain;

import net.result.sandnode.TestAgentConfig;
import net.result.sandnode.encryption.AsymmetricEncryptions;
import net.result.sandnode.encryption.KeyStorageRegistry;
import net.result.sandnode.hubagent.Agent;
import org.jetbrains.annotations.NotNull;

public class TestAgent extends Agent {
        public TestAgent() {
            super(new KeyStorageRegistry(AsymmetricEncryptions.ECIES.generate()), new TestAgentConfig());
        }

        @SuppressWarnings("DataFlowIssue")
        @Override
        protected @NotNull ServerChainManager createChainManager() {
            return null;
        }
    }