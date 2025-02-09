package net.result.sandnode.compression;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

public enum Compressions implements Compression {
    NONE {
        @Override
        public byte[] compress(byte[] data) {
            return data;
        }

        @Override
        public byte[] decompress(byte[] compressedData) {
            return compressedData;
        }
    },
    DEFLATE {
        @Override
        public byte[] compress(byte[] data) throws IOException {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            try (DeflaterOutputStream deflaterOutputStream = new DeflaterOutputStream(byteArrayOutputStream)) {
                deflaterOutputStream.write(data);
            }
            return byteArrayOutputStream.toByteArray();
        }

        @Override
        public byte[] decompress(byte[] compressedData) throws IOException {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            try (InflaterInputStream inflaterInputStream =
                         new InflaterInputStream(new ByteArrayInputStream(compressedData))) {
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = inflaterInputStream.read(buffer)) != -1) {
                    byteArrayOutputStream.write(buffer, 0, bytesRead);
                }
            }
            return byteArrayOutputStream.toByteArray();
        }
    }
}
