package net.result.sandnode.config;

import net.result.sandnode.encryption.interfaces.KeyStorage;

import java.util.UUID;

public record KeyEntry(UUID id, KeyStorage keyStorage) {}
