package net.result.sandnode.compression;

import net.result.sandnode.exception.DeserializationException;
import net.result.sandnode.exception.SerializationException;

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
        public byte[] compress(byte[] data) {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            try (DeflaterOutputStream deflaterOutputStream = new DeflaterOutputStream(byteArrayOutputStream)) {
                deflaterOutputStream.write(data);
            } catch (IOException e) {
                throw new SerializationException(e);
            }
            return byteArrayOutputStream.toByteArray();
        }

        @Override
        public byte[] decompress(byte[] compressedData) {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            try (InflaterInputStream in = new InflaterInputStream(new ByteArrayInputStream(compressedData))) {
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = in.read(buffer)) != -1) {
                    byteArrayOutputStream.write(buffer, 0, bytesRead);
                }
            } catch (IOException e) {
                throw new DeserializationException(e);
            }
            return byteArrayOutputStream.toByteArray();
        }
    }
}
