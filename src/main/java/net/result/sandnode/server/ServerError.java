package net.result.sandnode.server;

import net.result.sandnode.messages.types.ErrorMessage;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public enum ServerError {

    // General server errors
    SERVER_ERROR(1000, "Server"),
    UNKNOWN(1001, "Unknown"),
    DECODE(1002, "Decode"),
    JSON_PARSING(1003, "JSON Parsing"),
    TOO_FEW_ARGS(1004, "Too few arguments"),

    // Encryption-related errors
    INCORRECT_ENCRYPTION(2000, "Incorrect encryption"),
    UNKNOWN_ENCRYPTION(2001, "Unknown encryption method"),
    ENCRYPT(2002, "Encryption"),
    DECRYPT(2003, "Decryption"),
    KEY_NOT_FOUND(2004, "Key not found"),

    // Member-related errors
    INVALID_TOKEN(3000, "Invalid token"),
    MEMBER_NOT_FOUND(3001, "Member not found"),
    INVALID_MEMBER_ID_OR_PASSWORD(3002, "Invalid Member ID or password"),
    MEMBER_ID_BUSY(3003, "Member ID is already in use"),
    UNAUTHORIZED(3004, "Member unauthorized");

    public final int code;
    public final String desc;

    ServerError(int code, @NotNull String desc) {
        this.code = code;
        this.desc = desc;
    }

    @Contract(" -> new")
    public @NotNull ErrorMessage message() {
        return new ErrorMessage(this);
    }
}
