package net.result.sandnode.exception.error;

public class BusyNicknameException extends SpecialErrorException {
    public static final String SPECIAL = "busy";

    public BusyNicknameException() {
        super(SPECIAL);
    }

    public BusyNicknameException(String message) {
        super(SPECIAL, message);
    }

    public BusyNicknameException(String message, Throwable e) {
        super(SPECIAL, message, e);
    }

    public BusyNicknameException(Throwable e) {
        super(SPECIAL, e);
    }
}
