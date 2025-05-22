package net.result.taulight.dto;

import net.result.sandnode.encryption.interfaces.AsymmetricKeyStorage;

import java.util.UUID;

public record KeyDTO(UUID keyID, AsymmetricKeyStorage keyStorage) {}