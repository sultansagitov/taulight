package net.result.main.commands.sandnode;

import net.result.main.commands.CommandInfo;
import net.result.main.commands.CommandRegistry;
import net.result.main.commands.ConsoleContext;
import net.result.sandnode.chain.sender.LoginClientChain;
import net.result.sandnode.chain.sender.LogoutClientChain;
import net.result.sandnode.dto.LoginHistoryDTO;
import net.result.sandnode.encryption.interfaces.KeyStorage;
import net.result.sandnode.hubagent.Agent;
import net.result.sandnode.serverclient.SandnodeClient;

import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Comparator;
import java.util.List;

public class AuthCommands {
    public static void register(CommandRegistry registry) {
        registry.register(new CommandInfo("logout", "Log out of the account", "Auth", AuthCommands::logout));
        registry.register(new CommandInfo("loginHistory", "Show login history", "Auth", AuthCommands::loginHistory));
    }

    private static void logout(List<String> ignored, ConsoleContext context) throws Exception {
        var chain = new LogoutClientChain(context.client);
        context.io.chainManager.linkChain(chain);
        chain.logout();
        context.io.chainManager.removeChain(chain);
    }

    private static void loginHistory(List<String> ignored, ConsoleContext context) throws Exception {
        SandnodeClient client = context.client;
        var chain = new LoginClientChain(client);
        context.io.chainManager.linkChain(chain);
        var history = chain.getHistory();
        context.io.chainManager.removeChain(chain);
        var formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z");

        Agent agent = client.node().agent();

        for (LoginHistoryDTO dto : history.stream().sorted(Comparator.comparing(a -> a.time)).toList()) {
            KeyStorage personalKey = agent.config.loadPersonalKey(client.address, client.nickname);

            String ip = personalKey.decrypt(Base64.getDecoder().decode(dto.ip));
            String device = personalKey.decrypt(Base64.getDecoder().decode(dto.device));

            System.out.printf(
                    "Time: %s, IP: %s, Device: %s, Active: %s%n",
                    dto.time.format(formatter),
                    ip,
                    device,
                    dto.isOnline
            );
        }
    }
}
