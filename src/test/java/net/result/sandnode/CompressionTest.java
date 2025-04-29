package net.result.sandnode;

import net.result.sandnode.compression.Compression;
import net.result.sandnode.compression.CompressionManager;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.security.SecureRandom;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

public class CompressionTest {
    @Test
    public void test() throws IOException {
        for (Compression compression : CompressionManager.instance().list) {
            Random random = new SecureRandom();
            byte[] piece = new byte[500];
            random.nextBytes(piece);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            out.write(piece);
            out.write(piece);
            byte[] bytes = out.toByteArray();
            byte[] compressed = compression.compress(bytes);
            byte[] decompressed = compression.decompress(compressed);

            assertArrayEquals(bytes, decompressed, "Decompressed data does not match for " + compression.getClass());
        }
    }
}
