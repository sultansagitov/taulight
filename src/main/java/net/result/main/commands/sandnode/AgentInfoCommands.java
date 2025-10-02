package net.result.main.commands.sandnode;

import net.result.main.commands.CommandInfo;
import net.result.main.commands.CommandRegistry;
import net.result.main.commands.ConsoleContext;
import net.result.sandnode.chain.sender.HubInfoClientChain;
import net.result.sandnode.chain.sender.WhoAmIClientChain;

import java.util.List;

public class AgentInfoCommands {

    public static void register(CommandRegistry registry) {
        registry.register(new CommandInfo("whoami", "Show your nickname", "AgentInfo", AgentInfoCommands::whoami));
        registry.register(new CommandInfo("hubName", "Show hub name", "AgentInfo", AgentInfoCommands::name));
    }

    private static void whoami(List<String> ignored, ConsoleContext context) {
        WhoAmIClientChain chain = new WhoAmIClientChain(context.client);
        context.io.chainManager.linkChain(chain);
        String userID = chain.getNickname();
        context.io.chainManager.removeChain(chain);
        System.out.println(userID);
    }

    private static void name(List<String> ignored, ConsoleContext context) {
        HubInfoClientChain chain = new HubInfoClientChain(context.client);
        context.io.chainManager.linkChain(chain);
        System.out.printf("Hub name: %s%n", chain.get(true, false).name());
        context.io.chainManager.removeChain(chain);
    }
}
