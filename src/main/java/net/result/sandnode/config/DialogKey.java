package net.result.sandnode.config;

import net.result.sandnode.encryption.interfaces.KeyStorage;

import java.util.UUID;

public record DialogKey(UUID id, KeyStorage keyStorage) {}
