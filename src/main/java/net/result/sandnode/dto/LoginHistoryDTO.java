package net.result.sandnode.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import net.result.sandnode.db.LoginEntity;
import org.jetbrains.annotations.NotNull;

import java.time.ZonedDateTime;
import java.util.UUID;

public class LoginHistoryDTO {
    @JsonProperty
    public ZonedDateTime time;
    @JsonProperty
    public UUID encryptorID;
    @JsonProperty
    public String ip;
    @JsonProperty
    public String device;
    @JsonProperty("online")
    public boolean isOnline;

    @SuppressWarnings("unused")
    public LoginHistoryDTO() {}

    public LoginHistoryDTO(ZonedDateTime time, UUID encryptorID, String ip, String device, boolean isOnline) {
        this.time = time;
        this.encryptorID = encryptorID;
        this.ip = ip;
        this.device = device;
        this.isOnline = isOnline;
    }

    public LoginHistoryDTO(@NotNull LoginEntity e, boolean isOnline) {
        this(e.creationDate(), e.encryptor().id(), e.ip(), e.device(), isOnline);
    }
}
