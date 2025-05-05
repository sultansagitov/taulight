package net.result.sandnode.message.types;

import net.result.sandnode.dto.FileDTO;
import net.result.sandnode.encryption.interfaces.KeyStorage;
import net.result.sandnode.exception.ExpectedMessageException;
import net.result.sandnode.exception.FSException;
import net.result.sandnode.exception.crypto.CryptoException;
import net.result.sandnode.exception.error.EncryptionException;
import net.result.sandnode.message.Message;
import net.result.sandnode.message.RawMessage;
import net.result.sandnode.message.util.Headers;
import net.result.sandnode.message.util.MessageTypes;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class FileMessage extends Message {
    private static final Logger LOGGER = LogManager.getLogger(FileMessage.class);
    private final byte[] body;
    private final String filename;

    public FileMessage(@NotNull Headers headers, String path) throws FSException {
        this(headers, Paths.get(path).getFileName().toString(), loadFileBody(path));
    }

    public FileMessage(@NotNull Headers headers, String path, @NotNull KeyStorage keyStorage)
            throws EncryptionException, CryptoException, FSException {
        this(
                headers,
                Paths.get(path).getFileName().toString(),
                keyStorage.encryption().encryptBytes(loadFileBody(path), keyStorage)
        );
    }

    public FileMessage(@NotNull Headers headers, String filename, byte[] body) {
        super(headers.setType(MessageTypes.FILE).setValue("file", filename));
        this.filename = filename;
        this.body = body;
    }

    public FileMessage(@NotNull RawMessage raw) throws ExpectedMessageException {
        super(raw.expect(MessageTypes.FILE).headers());
        filename = headers().getValue("file");
        body = raw.getBody();
    }

    private static byte @NotNull [] loadFileBody(String path) throws FSException {
        try {
            return Files.readAllBytes(Paths.get(path));
        } catch (IOException e) {
            LOGGER.error("Failed to read file at path: {}", path, e);
            throw new FSException(e);
        }
    }

    public FileDTO dto() {
        return new FileDTO(filename(), getBody());
    }

    public String filename() {
        return filename;
    }

    @Override
    public byte[] getBody() {
        return body;
    }
}
