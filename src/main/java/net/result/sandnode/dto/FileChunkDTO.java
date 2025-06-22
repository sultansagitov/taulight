package net.result.sandnode.dto;

import java.util.UUID;

public record FileChunkDTO(
    UUID id,
    String contentType,
    byte[] body,
    int sequence,
    int totalChunks
) {}
