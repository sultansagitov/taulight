package net.result.sandnode.exception.error;

public class ExpiredTokenException extends SpecialErrorException {
    public static final String SPECIAL = "expired";

    public ExpiredTokenException(Throwable e) {
        super(SPECIAL, "Token is expired", e);
    }
}
