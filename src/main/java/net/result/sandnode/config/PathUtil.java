package net.result.sandnode.config;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.File;
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

}
