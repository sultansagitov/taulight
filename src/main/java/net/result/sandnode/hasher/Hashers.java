package net.result.sandnode.hasher;

import net.result.sandnode.exception.ImpossibleRuntimeException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public enum Hashers implements Hasher {
    SHA256 {
        @Override
        public @NotNull String hash(@NotNull String data) {
            return hash(data.getBytes(StandardCharsets.UTF_8));
        }

        @NotNull
        private String hash(byte @NotNull [] data) {
            MessageDigest md;
            try {
                md = MessageDigest.getInstance("SHA-256");
            } catch (NoSuchAlgorithmException e) {
                LOGGER.error(e);
                throw new ImpossibleRuntimeException(e);
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
    },
    MD5 {
        @Override
        public @NotNull String hash(@NotNull String data) {
            return hash(data.getBytes(StandardCharsets.UTF_8));
        }

        @NotNull
        private String hash(byte @NotNull [] data) {
            MessageDigest md;
            try {
                md = MessageDigest.getInstance("MD5");
            } catch (NoSuchAlgorithmException e) {
                LOGGER.error(e);
                throw new ImpossibleRuntimeException(e);
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
    };

    private static final Logger LOGGER = LogManager.getLogger(Hashers.class);
}
