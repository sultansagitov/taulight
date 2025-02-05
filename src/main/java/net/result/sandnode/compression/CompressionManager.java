package net.result.sandnode.compression;

import net.result.sandnode.util.Manager;

import java.util.Arrays;

public class CompressionManager extends Manager<Compression> {
    private static final CompressionManager INSTANCE = new CompressionManager();

    private CompressionManager() {
        Arrays.stream(Compressions.values()).forEach(this::add);
    }

    public static CompressionManager instance() {
        return INSTANCE;
    }

    @Override
    protected void handleOverflow(Compression compression) {
        list.removeIf(e -> e.name().equals(compression.name()));
    }
}
