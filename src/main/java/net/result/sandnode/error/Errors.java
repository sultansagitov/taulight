package net.result.sandnode.error;

import net.result.sandnode.message.types.ErrorMessage;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public enum Errors implements SandnodeError {
    // General server errors
    SERVER_ERROR(1000, "Server"),
    UNKNOWN(1001, "Unknown"),
    DECODE(1002, "Decode"),
    PARSING(1003, "Parsing"),
    TOO_FEW_ARGS(1004, "Too few arguments"),
    WRONG_ADDRESS(1005, "Wrong address"),

    // Encryption-related errors
    INCORRECT_ENCRYPTION(2000, "Incorrect encryption"),
    UNKNOWN_ENCRYPTION(2001, "Unknown encryption method"),
    ENCRYPT(2002, "Encryption"),
    DECRYPT(2003, "Decryption"),
    KEY_NOT_FOUND(2004, "Key not found"),

    // Member-related errors
    INVALID_TOKEN(3000, "Invalid token"),
    EXPIRED_TOKEN(3001, "Expired token"),
    MEMBER_NOT_FOUND(3002, "Member not found"),
    INVALID_MEMBER_ID_OR_PASSWORD(3003, "Invalid Member ID or password"),
    MEMBER_ID_BUSY(3004, "Member ID is already in use"),
    UNAUTHORIZED(3005, "Member unauthorized"),
    ADDRESSED_MEMBER_NOT_FOUND(3006, "Addressed member not found");

    private final int code;
    private final String desc;

    Errors(int code, @NotNull String desc) {
        this.code = code;
        this.desc = desc;
    }

    @Override
    @Contract(" -> new")
    public @NotNull ErrorMessage message() {
        return new ErrorMessage(this);
    }

    @Override
    public int getCode() {
        return code;
    }

    @Override
    public String getDescription() {
        return desc;
    }
}
