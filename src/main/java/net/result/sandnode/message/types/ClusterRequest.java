package net.result.sandnode.message.types;

import net.result.sandnode.exception.ExpectedMessageException;
import net.result.sandnode.message.RawMessage;
import net.result.sandnode.message.BaseMessage;
import net.result.sandnode.message.util.Headers;
import net.result.sandnode.message.util.MessageTypes;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Unmodifiable;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class ClusterRequest extends BaseMessage {
    private static final Logger LOGGER = LogManager.getLogger(ClusterRequest.class);

    @Unmodifiable
    private final Collection<String> clustersID;

    public ClusterRequest(@NotNull Headers headers, @NotNull Collection<String> clustersID) {
        super(headers.setType(MessageTypes.CLUSTER));
        Set<String> filteredClusterNames = new HashSet<>();
        for (String s : clustersID) {
            if (s == null || s.trim().isEmpty()) {
                LOGGER.warn("Skipping invalid cluster name: null or empty");
                continue;
            }

            String str = s.trim().toLowerCase();

            if (!str.matches("^#?[a-z0-9_]+$")) {
                LOGGER.warn("Skipping invalid cluster name: {}", s);
                continue;
            }

            filteredClusterNames.add(str);
        }
        this.clustersID = filteredClusterNames;
    }

    public ClusterRequest(@NotNull Collection<String> clustersID) {
        this(new Headers(), clustersID);
    }

    public ClusterRequest(@NotNull RawMessage message) throws ExpectedMessageException {
        super(message.expect(MessageTypes.CLUSTER).headers());

        String[] clustersID = new String(message.getBody()).split(",");
        this.clustersID = Arrays
                .stream(clustersID)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(String::toLowerCase)
                .filter(str -> str.matches("^#?[a-z0-9_]+$"))
                .map(str -> str.startsWith("#") ? str : "#" + str)
                .collect(Collectors.toSet());
    }

    public Collection<String> getClustersID() {
        return clustersID;
    }

    public String toString() {
        return super.toString() + " " + clustersID;
    }

    @Override
    public byte[] getBody() {
        return String.join(",", clustersID).getBytes();
    }
}
