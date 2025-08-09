package net.result.main.commands.sandnode;

import net.result.main.commands.LoopCondition;
import net.result.main.commands.ConsoleContext;
import net.result.sandnode.chain.Chain;
import net.result.sandnode.chain.ChainStorage;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class ChainsCommands {
    public static void register(Map<String, LoopCondition> commands) {
        commands.put("chains", ChainsCommands::chains);
    }

    private static void chains(List<String> ignored, ConsoleContext context) {
        ChainStorage storage = context.io.chainManager.storage();
        Collection<Chain> chains = storage.getAll();
        Map<String, Chain> map = storage.getNamed();

        System.out.printf("All client chains: %s%n", chains);
        System.out.printf("All named client chains: %s%n", map);
    }
}
