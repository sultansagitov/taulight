package net.result.sandnode.error;

import net.result.sandnode.exception.error.*;
import net.result.sandnode.message.types.ErrorMessage;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public enum Errors implements SandnodeError {
    // General server errors
    SERVER_ERROR(1000, "Server", ServerSandnodeErrorException::new),
    DECODE(1001, "Decode", DecodingErrorException::new),
    TOO_FEW_ARGS(1002, "Too few arguments", TooFewArgumentsException::new),
    INVALID_ARG(1003, "Invalid argument", InvalidArgumentException::new),
    WRONG_ADDR(1004, "Wrong address", WrongAddressException::new),
    UNHANDLED_MSG_TYPE(1005, "Unknown message type", UnhandledMessageTypeException::new),
    NOT_FOUND(1005, "Not found", NotFoundException::new),
    NO_EFFECT(1006, "No effect", NoEffectException::new),

    // Encryption-related errors
    INCORRECT_ENCRYPTION(2000, "Incorrect encryption", IncorrectEncryptionException::new),
    UNKNOWN_ENCRYPTION(2001, "Unknown encryption method", UnknownEncryptionException::new),
    ENCRYPT(2002, "Encryption", EncryptionException::new),
    DECRYPT(2003, "Decryption", DecryptionException::new),
    KEY_NOT_FOUND(2004, "Key not found", KeyStorageNotFoundException::new),

    // Member-related errors,
    EXPIRED_TOKEN(3000, "Expired token", ExpiredTokenException::new),
    INVALID_NICKNAME_OR_PASSWORD(3001, "Invalid nickname or password", InvalidNicknamePassword::new),
    BUSY_NICKNAME(3002, "Nickname is already in use", BusyNicknameException::new),
    UNAUTHORIZED(3003, "Member unauthorized", UnauthorizedException::new),
    ADDRESSED_MEMBER_NOT_FOUND(3004, "Addressed member not found", AddressedMemberNotFoundException::new);

    private final int code;
    private final String desc;
    private final Supplier<SandnodeErrorException> exceptionSupplier;

    Errors(int code, @NotNull String desc, Supplier<SandnodeErrorException> exceptionSupplier) {
        this.code = code;
        this.desc = desc;
        this.exceptionSupplier = exceptionSupplier;
    }
    @Override
    public int code() {
        return code;
    }

    @Override
    public String description() {
        return desc;
    }

    @Override
    @Contract(" -> new")
    public SandnodeErrorException exception() {
        return exceptionSupplier.get();
    }

    @Override
    @Contract(" -> new")
    public @NotNull ErrorMessage createMessage() {
        return new ErrorMessage(this);
    }
}
