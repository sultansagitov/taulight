package net.result.sandnode.util;

import net.result.sandnode.dto.FileChunkDTO;
import net.result.sandnode.dto.FileDTO;
import net.result.sandnode.message.Message;
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
        void send(@NotNull Message ignored);
    }

    @FunctionalInterface
    public interface ReceiveMethod {
        RawMessage receive();
    }

    public static void send(FileDTO dto, SendMethod method) {
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

    public static FileDTO receive(ReceiveMethod method) {

        Map<Integer, byte[]> chunks = new TreeMap<>();
        UUID fileId = null;
        String contentType = null;
        int totalChunks = -1;

        do {
            var raw = method.receive();

            var fileMessage = new FileMessage(raw);
            var chunk = fileMessage.chunk();

            if (fileId == null) fileId = chunk.id();
            if (contentType == null) contentType = chunk.contentType();
            if (totalChunks == -1) totalChunks = chunk.totalChunks();

            chunks.put(chunk.sequence(), chunk.body());
        } while (chunks.size() != totalChunks);

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
