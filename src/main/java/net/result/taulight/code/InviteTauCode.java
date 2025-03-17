package net.result.taulight.code;

import java.time.ZonedDateTime;

public class InviteTauCode implements TauCode {
    public String title;
    public ZonedDateTime expiresData;

    @SuppressWarnings("unused")
    public InviteTauCode() {
    }

    public InviteTauCode(String title, ZonedDateTime expiresData) {
        this.title = title;
        this.expiresData = expiresData;
    }
}
