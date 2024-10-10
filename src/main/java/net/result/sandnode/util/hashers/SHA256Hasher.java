package net.result.sandnode.util.hashers;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import static java.nio.charset.StandardCharsets.US_ASCII;

public class SHA256Hasher implements IHasher {
    private static final Logger LOGGER = LogManager.getLogger(SHA256Hasher.class);
    private static final SHA256Hasher instance = new SHA256Hasher();

    public static SHA256Hasher getInstance() {
        return instance;
    }

    @Override
    public @NotNull String hash(@NotNull String data) {
        return hash(data.getBytes(US_ASCII));
    }

    @Override
    public @NotNull String hash(byte @NotNull [] data) {
        MessageDigest md;
        try {
            md = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            LOGGER.error("I hope you never see this error in your logs", e);
            throw new RuntimeException(e);
        }
        byte[] digest = md.digest(data);

        // Hex encoding
        StringBuilder hexString = new StringBuilder();
        for (byte b : digest) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString().toUpperCase();
    }
}
