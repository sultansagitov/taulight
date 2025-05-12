package net.result.sandnode.util;

import net.result.sandnode.dto.FileDTO;
import net.result.sandnode.exception.error.InvalidArgumentException;
import net.result.sandnode.exception.error.NoEffectException;
import net.result.sandnode.exception.error.ServerSandnodeErrorException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

public class DBFileUtil {
    private static final Logger LOGGER = LogManager.getLogger(DBFileUtil.class);

    //TODO Replace with path from config
    private static final Path avatarDirectory = Paths.get(System.getProperty("user.home")).resolve("db/images");

    public DBFileUtil(Container container) {}

    public @NotNull String saveImage(FileDTO dto, UUID chatID) throws InvalidArgumentException, ServerSandnodeErrorException {
        String mime = dto.contentType();
        byte[] body = dto.body();

        if (!mime.startsWith("image/")) {
            throw new InvalidArgumentException();
        }

        String savedFilename = chatID.toString();

        try {
            if (!Files.exists(avatarDirectory)) {
                Files.createDirectories(avatarDirectory);
            }
        } catch (IOException e) {
            LOGGER.error("Failed to create directory: {}", avatarDirectory, e);
            throw new ServerSandnodeErrorException(e);
        }

        Path avatarPath = avatarDirectory.resolve(savedFilename);

        try {
            Files.write(avatarPath, body);
        } catch (IOException e) {
            LOGGER.error("Failed to save the avatar image for channel: {}", chatID, e);
            throw new ServerSandnodeErrorException(e);
        }
        return savedFilename;
    }

    public byte @NotNull [] readImage(String path) throws NoEffectException, ServerSandnodeErrorException {
        if (path == null) throw new NoEffectException();

        Path filePath = avatarDirectory.resolve(path);

        try {
            return Files.readAllBytes(filePath);
        } catch (IOException e) {
            throw new ServerSandnodeErrorException(e);
        }
    }
}
