package net.result.sandnode.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.ZonedDateTime;

public class LoginHistoryDTO {
    @JsonProperty
    public ZonedDateTime time;
    @JsonProperty
    public String ip;
    @JsonProperty
    public String device;
    @JsonProperty("online")
    public boolean isOnline;

    @SuppressWarnings("unused")
    public LoginHistoryDTO() {}

    public LoginHistoryDTO(ZonedDateTime time, String ip, String device, boolean isOnline) {
        this.time = time;
        this.ip = ip;
        this.device = device;
        this.isOnline = isOnline;
    }
}
