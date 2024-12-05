package net.result.sandnode.server;

import net.result.sandnode.exceptions.*;
import net.result.sandnode.messages.types.ErrorMessage;
import net.result.sandnode.messages.IMessage;
import net.result.sandnode.messages.util.Headers;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

public enum ServerError {
    UNKNOWN(2000, "Unknown"),
    DECODE(2002, "Decode"),
    JSON_PARSING(2010, "JSON Parsing"),
    TOO_FEW_ARGS(2012, "Too Few Args"),
    UNKNOWN_ENCRYPTION(2014, "Unknown Encryption"),

    ENCRYPT(2100, "Encrypt"),
    DECRYPT(2150, "Decrypt");


    private static final Logger LOGGER = LogManager.getLogger(ServerError.class);
    private final int code;
    private final String desc;

    ServerError(int code, @NotNull String desc) {
        this.code = code;
        this.desc = desc;
    }

    public void sendError(@NotNull Session session) throws EncryptionException, KeyStorageNotFoundException,
            MessageSerializationException, MessageWriteException, UnexpectedSocketDisconnectException {
        LOGGER.warn("Sending error with code {} (\"{}\" error) to client", code, desc);
        Headers headers = new Headers();
        IMessage response = new ErrorMessage(headers, code);
        session.io.sendMessage(response);
        LOGGER.info("Message was sent: {}", response);
    }
}