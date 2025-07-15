package net.result.sandnode.exception.error;

public class InvalidNicknamePassword extends SpecialErrorException {
    public static final String SPECIAL = "invalid.nick.pass";

    public InvalidNicknamePassword() {
        super(SPECIAL);
    }

    @SuppressWarnings("unused")
    public InvalidNicknamePassword(Throwable e) {
        super(SPECIAL, e);
    }
}
