package net.result.sandnode.compression;

import net.result.sandnode.message.util.Headers;
import net.result.sandnode.util.Manager;

import java.util.Arrays;
import java.util.Optional;

public class CompressionManager extends Manager<Compression> {
    private static final CompressionManager INSTANCE = new CompressionManager();
    public static final String HEADER_NAME = "comp";

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

    public Optional<Compression> find(String name) {
        return list.stream().filter(c -> c.name().equalsIgnoreCase(name)).findFirst();
    }

    public Optional<Compression> getFromHeaders(Headers headers) {
        return headers
                .getOptionalValue(HEADER_NAME)
                .map(s -> CompressionManager.instance().find(s).orElse(Compressions.NONE));
    }
}
