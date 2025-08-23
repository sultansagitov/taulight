package net.result.sandnode.db;

import net.result.sandnode.config.HubConfig;
import net.result.sandnode.dto.FileDTO;
import net.result.sandnode.entity.FileEntity;
import net.result.sandnode.exception.error.NoEffectException;
import net.result.sandnode.exception.error.ServerErrorException;
import net.result.sandnode.repository.FileRepository;
import net.result.sandnode.util.Container;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class DBFileUtil {
    private static final Logger LOGGER = LogManager.getLogger(DBFileUtil.class);

    private final FileRepository fileRepo;
    private final HubConfig hubConfig;

    public DBFileUtil(Container container) {
        fileRepo = container.get(FileRepository.class);
        hubConfig = container.get(HubConfig.class);
    }

    public @NotNull FileEntity saveFile(FileDTO dto, String filename) {
        Path avatarDirectory = hubConfig.imagePath();

        byte[] body = dto.body();

        try {
            if (!Files.exists(avatarDirectory)) {
                Files.createDirectories(avatarDirectory);
            }
        } catch (IOException e) {
            LOGGER.error("Failed to create directory: {}", avatarDirectory, e);
            throw new ServerErrorException(e);
        }

        try {
            Path avatarPath = avatarDirectory.resolve(filename);
            Files.write(avatarPath, body);
        } catch (IOException e) {
            LOGGER.error("Failed to save the avatar image for group: {}", filename, e);
            throw new ServerErrorException(e);
        }

        return fileRepo.create(dto.contentType(), filename);
    }

    public FileDTO readImage(FileEntity file) {
        Path avatarDirectory = hubConfig.imagePath();

        if (file == null) throw new NoEffectException();
        Path filePath = avatarDirectory.resolve(file.getFilename());

        try {
            return new FileDTO(file.id(), file.getContentType(), Files.readAllBytes(filePath));
        } catch (IOException e) {
            throw new ServerErrorException(e);
        }
    }
}
