package net.result.sandnode.util.hashers;

import net.result.sandnode.util.encodings.hex.HexEncoder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import static java.nio.charset.StandardCharsets.US_ASCII;

public class SHA256Hasher implements IHasher {
    private static final Logger LOGGER = LogManager.getLogger(SHA256Hasher.class);

    @Override
    public @NotNull String hash(@NotNull String data) {
        return hash(data.getBytes(US_ASCII));
    }

    @Override
    public @NotNull String hash(byte @NotNull [] data) {
        final MessageDigest md;
        try {
            md = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            LOGGER.error("I hope you never see this error in your logs", e);
            throw new RuntimeException(e);
        }
        final byte[] digest = md.digest(data);
        return new HexEncoder().encode(digest);
    }
}
