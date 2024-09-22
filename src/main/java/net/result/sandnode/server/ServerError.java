package net.result.sandnode.server;

import net.result.sandnode.messages.ErrorMessage;
import net.result.sandnode.messages.HeadersBuilder;
import net.result.sandnode.messages.IMessage;
import net.result.sandnode.messages.util.Connection;
import net.result.sandnode.server.commands.ICommand;
import net.result.sandnode.server.commands.ResponseCommand;
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
    RSA_ENCRYPT(2102, "RSA Encrypt"),
    AES_ENCRYPT(2104, "AES Encrypt"),

    DECRYPT(2150, "Decrypt"),
    RSA_DECRYPT(2152, "RSA Decrypt"),
    AES_DECRYPT(2154, "AES Decrypt");


    private static final Logger LOGGER = LogManager.getLogger(ServerError.class);
    private final int code;
    private final String desc;

    ServerError(int code, @NotNull String desc) {
        this.code = code;
        this.desc = desc;
    }

    public @NotNull ICommand sendError(@NotNull Connection opposite) {
        LOGGER.warn("Sending error with code {} (\"{}\" error) to client", code, desc);
        HeadersBuilder headersBuilder = new HeadersBuilder().set(opposite);
        IMessage response = new ErrorMessage(headersBuilder, code);
        return new ResponseCommand(response);
    }
}