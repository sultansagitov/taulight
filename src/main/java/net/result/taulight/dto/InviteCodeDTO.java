package net.result.taulight.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import net.result.taulight.db.InviteCodeEntity;
import org.jetbrains.annotations.Nullable;

import java.time.ZonedDateTime;

public class InviteCodeDTO extends CodeDTO {
    @JsonProperty
    public String title;
    @JsonProperty("receiver-nickname")
    public String receiverNickname;
    @JsonProperty("sender-nickname")
    public String senderNickname;
    @JsonProperty("creation-date")
    public ZonedDateTime creationDate;
    @JsonProperty("activation-date")
    public @Nullable ZonedDateTime activationDate;
    @JsonProperty("expires-date")
    public ZonedDateTime expiresDate;

    @SuppressWarnings("unused")
    public InviteCodeDTO() {
        super();
    }

    public InviteCodeDTO(InviteCodeEntity invite) {
        super(invite.code());
        this.title = invite.channel().title();
        this.receiverNickname = invite.receiver().nickname();
        this.senderNickname = invite.sender().nickname();
        creationDate = invite.creationDate();
        activationDate = invite.activationDate();
        expiresDate = invite.expiresDate();
    }
}
