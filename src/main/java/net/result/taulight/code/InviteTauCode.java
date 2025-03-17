package net.result.taulight.code;

import net.result.taulight.db.InviteToken;
import org.jetbrains.annotations.Nullable;

import java.time.ZonedDateTime;

public class InviteTauCode implements TauCode {
    public String title;
    public String nickname;
    public String senderNickname;
    public ZonedDateTime creationDate;
    public @Nullable ZonedDateTime activationDate;
    public ZonedDateTime expiresData;

    @SuppressWarnings("unused")
    public InviteTauCode() {
    }

    public InviteTauCode(InviteToken inviteToken, String title, String nickname, String senderNickname) {
        this.title = title;
        this.nickname = nickname;
        this.senderNickname = senderNickname;
        creationDate = inviteToken.getCreationDate();
        activationDate = inviteToken.getActivationDate();
        expiresData = inviteToken.getExpiresData();
    }
}
