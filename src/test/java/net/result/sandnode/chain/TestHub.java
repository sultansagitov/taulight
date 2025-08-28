package net.result.sandnode.chain;

import net.result.sandnode.config.HubConfigRecord;
import net.result.sandnode.encryption.KeyStorageRegistry;
import net.result.sandnode.hubagent.Hub;
import net.result.sandnode.security.PasswordHashers;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;

public class TestHub extends Hub {
    private final ServerChainManager serverCM;

    public TestHub(String name, ServerChainManager serverCM, KeyStorageRegistry hubKeyStorage) {
        super(hubKeyStorage, new HubConfigRecord(name, PasswordHashers.BCRYPT, Path.of("/")));
        this.serverCM = serverCM;
    }

    @Override
    public @NotNull ServerChainManager createChainManager() {
        return serverCM;
    }
}