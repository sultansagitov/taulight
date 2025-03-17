package net.result.sandnode.error;

import net.result.sandnode.exception.error.*;
import net.result.sandnode.message.types.ErrorMessage;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public enum Errors implements SandnodeError {
    // General server errors
    SERVER_ERROR(1000, "Server"),
    DECODE(1001, "Decode"),
    TOO_FEW_ARGS(1002, "Too few arguments"),
    WRONG_ADDRESS(1003, "Wrong address"),
    UNHANDLED_MESSAGE_TYPE(1004, "Unknown message type"),
    NOT_FOUND(1005, "Not found"),
    NO_EFFECT(1006, "No effect"),

    // Encryption-related errors
    INCORRECT_ENCRYPTION(2000, "Incorrect encryption"),
    UNKNOWN_ENCRYPTION(2001, "Unknown encryption method"),
    ENCRYPT(2002, "Encryption"),
    DECRYPT(2003, "Decryption"),
    KEY_NOT_FOUND(2004, "Key not found"),

    // Member-related errors,
    INVALID_TOKEN(3000, "Invalid token"),
    EXPIRED_TOKEN(3001, "Expired token"),
    INVALID_NICKNAME_OR_PASSWORD(3002, "Invalid nickname or password"),
    BUSY_NICKNAME(3003, "Nickname is already in use"),
    UNAUTHORIZED(3004, "Member unauthorized"),
    ADDRESSED_MEMBER_NOT_FOUND(3005, "Addressed member not found");

    private final int code;
    private final String desc;

    Errors(int code, @NotNull String desc) {
        this.code = code;
        this.desc = desc;
    }

    @Override
    @Contract(" -> new")
    public @NotNull ErrorMessage createMessage() {
        return new ErrorMessage(this);
    }

    @Override
    public int code() {
        return code;
    }

    @Override
    public String description() {
        return desc;
    }

    public static void throwHandler(SandnodeError error) throws SandnodeErrorException {
        if (error instanceof Errors err) {
            switch (err) {
                case SERVER_ERROR -> throw new ServerSandnodeErrorException();
                case DECODE -> throw new DecodingErrorException();
                case TOO_FEW_ARGS -> throw new TooFewArgumentsException();
                case WRONG_ADDRESS -> throw new WrongAddressException();
                case UNHANDLED_MESSAGE_TYPE -> throw new UnhandledMessageTypeException();
                case NOT_FOUND -> throw new NotFoundException();
                case NO_EFFECT -> throw new NoEffectException();

                case INCORRECT_ENCRYPTION -> throw new IncorrectEncryptionException();
                case UNKNOWN_ENCRYPTION -> throw new UnknownEncryptionException();
                case ENCRYPT -> throw new EncryptionException();
                case DECRYPT -> throw new DecryptionException();
                case KEY_NOT_FOUND -> throw new KeyStorageNotFoundException();

                case INVALID_TOKEN -> throw new InvalidTokenException();
                case EXPIRED_TOKEN -> throw new ExpiredTokenException();
                case INVALID_NICKNAME_OR_PASSWORD -> throw new InvalidNicknamePassword();
                case BUSY_NICKNAME -> throw new BusyNicknameException();
                case UNAUTHORIZED -> throw new UnauthorizedException();
                case ADDRESSED_MEMBER_NOT_FOUND -> throw new AddressedMemberNotFoundException();
            }
        }
    }
}
