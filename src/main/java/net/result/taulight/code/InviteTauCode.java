package net.result.taulight.code;

import com.fasterxml.jackson.annotation.JsonProperty;
import net.result.taulight.db.InviteCodeObject;
import org.jetbrains.annotations.Nullable;

import java.time.ZonedDateTime;

public class InviteTauCode extends TauCode {
    @JsonProperty
    public String title;
    @JsonProperty
    public String nickname;
    @JsonProperty("sender-nickname")
    public String senderNickname;
    @JsonProperty("creation-date")
    public ZonedDateTime creationDate;
    @JsonProperty("activation-date")
    public @Nullable ZonedDateTime activationDate;
    @JsonProperty("expires-date")
    public ZonedDateTime expiresDate;

    @SuppressWarnings("unused")
    public InviteTauCode() {
        super();
    }

    public InviteTauCode(InviteCodeObject inviteCode, String title, String nickname, String senderNickname) {
        super(inviteCode.getCode());
        this.title = title;
        this.nickname = nickname;
        this.senderNickname = senderNickname;
        creationDate = inviteCode.getCreationDate();
        activationDate = inviteCode.getActivationDate();
        expiresDate = inviteCode.getExpiresData();
    }
}
