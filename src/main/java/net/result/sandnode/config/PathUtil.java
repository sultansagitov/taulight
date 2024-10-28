package net.result.sandnode.config;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class PathUtil {
    private static final Logger LOGGER = LogManager.getLogger(PathUtil.class);

    static @NotNull Path resolveHomeInPath(@NotNull Path path) {
        String inputPath = path.toString();
        return resolveHomeInPath(inputPath);
    }

    static @NotNull Path resolveHomeInPath(@NotNull String path) {
        String homeDir = System.getProperty("user.home");
        int tildeIndex = path.indexOf("~");
        return tildeIndex < 0
                ? Path.of(path).toAbsolutePath()
                : Paths.get(homeDir, path.substring(tildeIndex + 1));
    }

    public static void createDir(@NotNull Path directoryPath) throws IOException {
        File dir = new File(directoryPath.toString());
        if (!dir.exists()) {
            LOGGER.warn("Directory \"{}\" not found, it will be created now", directoryPath);

            if (dir.mkdirs())
                LOGGER.info("Directory successfully created");
            else {
                IOException e = new IOException("Failed to create directory");
                LOGGER.error("Failed to create directory \"{}\"", directoryPath, e);
                throw e;
            }
        }
    }

    public static void createFile(@NotNull Path filePath, @NotNull String content) throws IOException {
        File file = new File(filePath.toString());
        if (!file.exists()) {
            LOGGER.warn("File \"{}\" not found, it will be created now", filePath);

            try {
                if (file.getParentFile() != null && !file.getParentFile().exists()) {
                    if (file.getParentFile().mkdirs()) {
                        LOGGER.info("Parent directory successfully created");
                    } else {
                        throw new IOException("Failed to create parent directory");
                    }
                }
                if (file.createNewFile()) {
                    LOGGER.info("File successfully created");

                    // Writing content to the file
                    try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
                        writer.write(content);
                        LOGGER.info("Content successfully written to file");
                    }
                } else {
                    throw new IOException("Failed to create file");
                }
            } catch (IOException e) {
                LOGGER.error("Failed to create file \"{}\"", filePath, e);
                throw e;
            }
        } else {
            LOGGER.info("File \"{}\" already exists", filePath);
        }
    }

}
