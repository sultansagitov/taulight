package net.result.sandnode.compression;

public interface Compression {

    String name();

    byte[] compress(byte[] data);

    byte[] decompress(byte[] compressedData);

}
