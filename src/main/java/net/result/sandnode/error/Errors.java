package net.result.sandnode.error;

import net.result.sandnode.exception.error.*;
import net.result.sandnode.message.types.ErrorMessage;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public enum Errors implements SandnodeError {
    // General server errors
    SPECIAL("Special", null),
    SERVER("Server", ServerErrorException::new),
    DECODE("Decode", DecodingErrorException::new),
    TOO_FEW_ARGS("Too few arguments", TooFewArgumentsException::new),
    INVALID_ARG("Invalid argument", InvalidArgumentException::new),
    WRONG_ADDR("Wrong address", WrongAddressException::new),
    UNHANDLED_MSG_TYPE("Unknown message type", UnhandledMessageTypeException::new),
    NOT_FOUND("Not found", NotFoundException::new),
    NO_EFFECT("No effect", NoEffectException::new),

    // Encryption-related errors
    INCORRECT_ENCRYPTION("Incorrect encryption", IncorrectEncryptionException::new),
    UNKNOWN_ENCRYPTION("Unknown encryption method", UnknownEncryptionException::new),
    ENCRYPT("Encryption", EncryptionException::new),
    DECRYPT("Decryption", DecryptionException::new),
    KEY_NOT_FOUND("Key not found", KeyStorageNotFoundException::new),

    // Member-related errors,
    EXPIRED_TOKEN("Expired token", ExpiredTokenException::new),
    INVALID_NICKNAME_OR_PASSWORD("Invalid nickname or password", InvalidNicknamePassword::new),
    BUSY_NICKNAME("Nickname is already in use", BusyNicknameException::new),
    UNAUTHORIZED("Member unauthorized", UnauthorizedException::new),
    ADDRESSED_MEMBER_NOT_FOUND("Addressed member not found", AddressedMemberNotFoundException::new);

    private final String code;
    private final String desc;
    private final Supplier<SandnodeErrorException> exceptionSupplier;

    Errors(@NotNull String desc, Supplier<SandnodeErrorException> exceptionSupplier) {
        this.code = "sandnode:%s".formatted(name().toLowerCase());
        this.desc = desc;
        this.exceptionSupplier = exceptionSupplier;
    }

    @Override
    public String code() {
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
