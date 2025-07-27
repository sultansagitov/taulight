package net.result.sandnode.dto;

import net.result.sandnode.encryption.interfaces.KeyStorage;

public record KeyDTO(String sender, KeyStorage keyStorage) {}