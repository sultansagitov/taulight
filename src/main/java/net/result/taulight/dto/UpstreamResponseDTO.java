package net.result.taulight.dto;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
public class UpstreamResponseDTO {
    public UUID id;
    public ZonedDateTime creationDate;
}
