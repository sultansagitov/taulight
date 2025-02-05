package net.result.sandnode.compression;

import java.io.IOException;

public interface Compression {

    String name();

    byte[] compress(byte[] data) throws IOException;

    byte[] decompress(byte[] compressedData) throws IOException;

}
