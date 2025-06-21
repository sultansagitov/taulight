package net.result.sandnode.config;

import net.result.sandnode.security.PasswordHasher;

import java.nio.file.Path;

public interface HubConfig {

    String name();

    PasswordHasher hasher();

    Path imagePath();
}
