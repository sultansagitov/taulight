package net.result.sandnode.dto;

import net.result.sandnode.encryption.interfaces.KeyStorage;

import java.util.UUID;

public record KeyDTO(UUID keyID, KeyStorage keyStorage) {}