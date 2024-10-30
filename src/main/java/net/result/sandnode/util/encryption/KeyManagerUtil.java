package net.result.sandnode.util.encryption;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Collections;

public class KeyManagerUtil {
    private static final Logger LOGGER = LogManager.getLogger(KeyManagerUtil.class);

    public static void setKeyFilePermissions(@NotNull Path filePath) throws IOException {
        Files.setPosixFilePermissions(filePath, Collections.singleton(PosixFilePermission.OWNER_READ));
        LOGGER.info("Permissions of file \"{}\" set to 400 (only owner read)", filePath);
    }

}
