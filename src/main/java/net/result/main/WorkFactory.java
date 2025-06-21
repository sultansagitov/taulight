package net.result.main;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

public class WorkFactory {
    private static final Logger LOGGER = LogManager.getLogger(WorkFactory.class);

    public static @NotNull IWork getWork(@NotNull String workName) {
        LOGGER.info("Argument: {}", workName);
        return switch (workName) {
            case "run-hub" -> new RunHubWork();
            case "run-agent" -> new RunAgentWork();
            case "gen-keys" -> new GenerateKeysWork();
            default -> throw new UnknownError();
        };
    }
}
