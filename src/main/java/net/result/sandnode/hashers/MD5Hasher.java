package net.result.sandnode.hashers;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import static java.nio.charset.StandardCharsets.US_ASCII;

public class MD5Hasher implements IHasher {
    private static final Logger LOGGER = LogManager.getLogger(MD5Hasher.class);
    private static final MD5Hasher INSTANCE = new MD5Hasher();

    public static MD5Hasher instance() {
        return INSTANCE;
    }

    @Override
    public @NotNull String hash(@NotNull String data) {
        return hash(data.getBytes(US_ASCII));
    }

    @Override
    public @NotNull String hash(byte @NotNull [] data) {
        MessageDigest md;
        try {
            md = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            LOGGER.error("I hope you never see this error in your logs", e);
            throw new RuntimeException(e);
        }

        byte[] digest = md.digest(data);

        // Hex Encoding
        StringBuilder hexString = new StringBuilder();
        for (byte b : digest) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString().toUpperCase();
    }
}
