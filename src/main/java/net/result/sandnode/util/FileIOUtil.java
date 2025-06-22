package net.result.sandnode.util;

import net.result.sandnode.dto.FileDTO;
import net.result.sandnode.dto.FileChunkDTO;
import net.result.sandnode.error.ServerErrorManager;
import net.result.sandnode.exception.ExpectedMessageException;
import net.result.sandnode.exception.UnknownSandnodeErrorException;
import net.result.sandnode.exception.UnprocessedMessagesException;
import net.result.sandnode.exception.error.SandnodeErrorException;
import net.result.sandnode.message.IMessage;
import net.result.sandnode.message.RawMessage;
import net.result.sandnode.message.types.FileMessage;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

public class FileIOUtil {
    @FunctionalInterface
    public interface SendMethod {
        void send(@NotNull IMessage request) throws UnprocessedMessagesException, InterruptedException;
    }

    @FunctionalInterface
    public interface ReceiveMethod {
        RawMessage receive() throws InterruptedException;
    }

    public static void send(FileDTO dto, SendMethod method) throws UnprocessedMessagesException, InterruptedException {
        final int CHUNK_SIZE = 1024 * 1024;
        byte[] body = dto.body();
        UUID id = dto.id() != null ? dto.id() : UUID.randomUUID();
        int totalChunks = (int) Math.ceil((double) body.length / CHUNK_SIZE);

        for (int i = 0; i < totalChunks; i++) {
            int start = i * CHUNK_SIZE;
            int end = Math.min(start + CHUNK_SIZE, body.length);
            byte[] chunk = new byte[end - start];
            System.arraycopy(body, start, chunk, 0, chunk.length);

            FileChunkDTO chunkDTO = new FileChunkDTO(id, dto.contentType(), chunk, i, totalChunks);
            method.send(new FileMessage(chunkDTO));
        }
    }

    public static FileDTO receive(ReceiveMethod method) throws InterruptedException, ExpectedMessageException,
            UnknownSandnodeErrorException, SandnodeErrorException {

        Map<Integer, byte[]> chunks = new TreeMap<>();
        UUID fileId = null;
        String contentType = null;
        int totalChunks = -1;

        while (true) {
            RawMessage raw = method.receive();
            ServerErrorManager.instance().handleError(raw);

            FileMessage fileMessage = new FileMessage(raw);
            FileChunkDTO chunk = fileMessage.chunk();

            if (fileId == null) fileId = chunk.id();
            if (contentType == null) contentType = chunk.contentType();
            if (totalChunks == -1) totalChunks = chunk.totalChunks();

            chunks.put(chunk.sequence(), chunk.body());

            if (chunks.size() == totalChunks) break;
        }

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        for (byte[] chunk : chunks.values()) {
            try {
                output.write(chunk);
            } catch (IOException e) {
                throw new RuntimeException("Error reconstructing file", e);
            }
        }

        return new FileDTO(fileId, contentType, output.toByteArray());
    }
}
