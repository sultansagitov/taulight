package net.result.taulight.dto;

import net.result.sandnode.encryption.interfaces.AsymmetricKeyStorage;

import java.util.UUID;

public record DialogKeyDTO(UUID keyID, AsymmetricKeyStorage keyStorage) {}