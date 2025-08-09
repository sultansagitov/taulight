package net.result.main;

import net.result.main.agent.RunAgentWork;
import net.result.main.hub.RunHubWork;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

public class WorkFactory {
    private static final Logger LOGGER = LogManager.getLogger(WorkFactory.class);

    public static @NotNull Work getWork(@NotNull String workName) {
        LOGGER.info("Argument: {}", workName);
        return switch (workName) {
            case "run-hub" -> new RunHubWork();
            case "run-agent" -> new RunAgentWork();
            case "gen-keys" -> new GenerateKeysWork();
            default -> throw new UnknownError();
        };
    }
}
