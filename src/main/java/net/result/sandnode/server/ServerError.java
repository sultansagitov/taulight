package net.result.sandnode.server;

import net.result.sandnode.messages.IMessage;
import net.result.sandnode.messages.types.ErrorMessage;
import org.jetbrains.annotations.NotNull;

public enum ServerError {
    SERVER_ERROR(0, "Server error"),
    UNKNOWN(2000, "Unknown"),
    DECODE(2002, "Decode"),
    JSON_PARSING(2010, "JSON Parsing"),
    TOO_FEW_ARGS(2012, "Too Few Args"),
    INCORRECT_ENCRYPTION(2014, "Incorrect Encryption"),
    UNKNOWN_ENCRYPTION(2014, "Unknown Encryption"),
    MEMBER_NOT_FOUND(2015, "Member not found"),

    ENCRYPT(2100, "Encrypt"),
    DECRYPT(2150, "Decrypt"),
    MEMBER_ID_BUSY(378, "MemberID is busy");

    public final int code;
    public final String desc;

    ServerError(int code, @NotNull String desc) {
        this.code = code;
        this.desc = desc;
    }

    public IMessage message() {
        return new ErrorMessage(this);
    }
}