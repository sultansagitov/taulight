package net.result.sandnode.util;

import net.result.sandnode.config.HubConfig;
import net.result.sandnode.db.FileEntity;
import net.result.sandnode.db.FileRepository;
import net.result.sandnode.dto.FileDTO;
import net.result.sandnode.exception.DatabaseException;
import net.result.sandnode.exception.error.InvalidArgumentException;
import net.result.sandnode.exception.error.NoEffectException;
import net.result.sandnode.exception.error.ServerSandnodeErrorException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class DBFileUtil {
    private static final Logger LOGGER = LogManager.getLogger(DBFileUtil.class);

    private final FileRepository fileRepo;
    private final Path avatarDirectory;

    public DBFileUtil(Container container) {
        fileRepo = container.get(FileRepository.class);
        avatarDirectory = container.get(HubConfig.class).imagePath();
    }

    public @NotNull FileEntity saveImage(FileDTO dto, String filename)
            throws InvalidArgumentException, ServerSandnodeErrorException, DatabaseException {
        String mime = dto.contentType();
        byte[] body = dto.body();

        if (!mime.startsWith("image/")) {
            throw new InvalidArgumentException();
        }

        try {
            if (!Files.exists(avatarDirectory)) {
                Files.createDirectories(avatarDirectory);
            }
        } catch (IOException e) {
            LOGGER.error("Failed to create directory: {}", avatarDirectory, e);
            throw new ServerSandnodeErrorException(e);
        }

        try {
            Path avatarPath = avatarDirectory.resolve(filename);
            Files.write(avatarPath, body);
        } catch (IOException e) {
            LOGGER.error("Failed to save the avatar image for channel: {}", filename, e);
            throw new ServerSandnodeErrorException(e);
        }

        return fileRepo.create(dto.contentType(), filename);
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
