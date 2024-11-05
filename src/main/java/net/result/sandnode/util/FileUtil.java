package net.result.sandnode.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystemException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Collections;

public class FileUtil {
    private static final Logger LOGGER = LogManager.getLogger(FileUtil.class);

    public static @NotNull Path resolveHomeInPath(@NotNull String path) {
        String homeDir = System.getProperty("user.home");
        int tildeIndex = path.indexOf("~");
        return tildeIndex < 0
                ? Path.of(path).toAbsolutePath()
                : Paths.get(homeDir, path.substring(tildeIndex + 1));
    }

    public static @NotNull Path resolveHomeInPath(@NotNull Path path) {
        String inputPath = path.toString();
        return resolveHomeInPath(inputPath);
    }

    public static void createDir(@NotNull Path directoryPath) throws IOException {
        File dir = directoryPath.toFile();
        if (!dir.exists()) {
            LOGGER.warn("Directory \"{}\" not found, so it will be created now", directoryPath);

            if (dir.mkdirs())
                LOGGER.info("Directory successfully created");
            else {
                IOException e = new IOException("Failed to create directory");
                LOGGER.error("Failed to create directory \"{}\"", directoryPath, e);
                throw e;
            }
        }
    }

    public static void makeOwnerOnlyRead(@NotNull Path filePath) throws IOException {
        Files.setPosixFilePermissions(filePath, Collections.singleton(PosixFilePermission.OWNER_READ));
        LOGGER.info("Permissions of file \"{}\" set to 400 (only owner read)", filePath);
    }

    public static void createFile(@NotNull Path path) throws IOException {
        if (path.toFile().createNewFile()) {
            LOGGER.info("File created \"{}\"", path);
        } else {
            LOGGER.info("Cannot create file \"{}\"", path);
            throw new IOException(String.format("Cannot create file \"%s\"", path));
        }
    }

    public static void checkNotDirectory(@NotNull Path keysJsonPath) throws FileSystemException {
        if (Files.isDirectory(keysJsonPath)) {
            LOGGER.error("\"{}\" is a directory", keysJsonPath);
            throw new FileSystemException(String.format("\"%s\" is a directory", keysJsonPath));
        }
    }
}
