package net.result.main.commands.sandnode;

import net.result.main.commands.CommandInfo;
import net.result.main.commands.CommandRegistry;
import net.result.main.commands.ConsoleContext;
import net.result.sandnode.chain.sender.NameClientChain;
import net.result.sandnode.chain.sender.WhoAmIClientChain;

import java.util.List;

public class UserInfoCommands {

    public static void register(CommandRegistry registry) {
        registry.register(new CommandInfo("whoami", "Show your nickname", "UserInfo", UserInfoCommands::whoami));
        registry.register(new CommandInfo("name", "Show hub name", "UserInfo", UserInfoCommands::name));
    }

    private static void whoami(List<String> ignored, ConsoleContext context) throws Exception {
        WhoAmIClientChain chain = new WhoAmIClientChain(context.client);
        context.io.chainManager.linkChain(chain);
        String userID = chain.getNickname();
        context.io.chainManager.removeChain(chain);
        System.out.println(userID);
    }

    private static void name(List<String> ignored, ConsoleContext context) throws Exception {
        NameClientChain chain = new NameClientChain(context.client);
        context.io.chainManager.linkChain(chain);
        System.out.printf("Hub name: %s%n", chain.getName());
        context.io.chainManager.removeChain(chain);
    }
}
