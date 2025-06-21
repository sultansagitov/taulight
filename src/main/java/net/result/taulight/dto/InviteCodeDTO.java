package net.result.taulight.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import net.result.taulight.db.InviteCodeEntity;

/**
 * Data Transfer Object representing an invite code along with metadata about sender, receiver, and validity.
 */
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

    /** Default constructor. */
    @SuppressWarnings("unused")
    public InviteCodeDTO() {
        super();
    }

    /**
     * Constructs an InviteCodeDTO from an {@link InviteCodeEntity}.
     *
     * @param invite the invite entity
     */
    public InviteCodeDTO(InviteCodeEntity invite) {
        super(invite.code());
        title = invite.group().title();
        receiverNickname = invite.receiver().member().nickname();
        senderNickname = invite.sender().member().nickname();
        creationDate = invite.creationDate();
        activationDate = invite.activationDate();
        expiresDate = invite.expiresDate();
    }
}
