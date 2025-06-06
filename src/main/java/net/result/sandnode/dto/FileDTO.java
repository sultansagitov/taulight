package net.result.sandnode.dto;

import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public record FileDTO(@Nullable UUID id, String contentType, byte[] body) {
}
