package net.result.sandnode.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

@NoArgsConstructor
@AllArgsConstructor
public class LoginHistoryDTO {
    @JsonProperty
    public ZonedDateTime time;
    @JsonProperty
    public String ip;
    @JsonProperty
    public String device;
    @JsonProperty("online")
    public boolean isOnline;
}
