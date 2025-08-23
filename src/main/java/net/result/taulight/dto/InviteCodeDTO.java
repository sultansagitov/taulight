package net.result.taulight.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

/**
 * Data Transfer Object representing an invite code along with metadata about sender, receiver, and validity.
 */
@NoArgsConstructor
public class InviteCodeDTO extends CodeDTO {
    /** Title of the associated group. */
    @JsonProperty
    public String title;
    /** Nickname of the invited user. */
    @JsonProperty("receiver-nickname")
    public String receiverNickname;
    /** Nickname of the sender. */
    @JsonProperty("sender-nickname")
    public String senderNickname;

    public InviteCodeDTO(
            String code,
            String title,
            String receiverNickname,
            String senderNickname,
            ZonedDateTime creationDate,
            ZonedDateTime activationDate,
            ZonedDateTime expiresDate
    ) {
        super(code);
        this.title = title;
        this.receiverNickname = receiverNickname;
        this.senderNickname = senderNickname;
        this.creationDate = creationDate;
        this.activationDate = activationDate;
        this.expiresDate = expiresDate;
    }
}
