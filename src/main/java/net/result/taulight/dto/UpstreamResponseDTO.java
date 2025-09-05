package net.result.taulight.dto;

import java.time.ZonedDateTime;
import java.util.UUID;

public record UpstreamResponseDTO(UUID id, ZonedDateTime creationDate) {}
