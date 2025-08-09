package net.result.main.agent;

import net.result.main.Work;
import net.result.main.chain.ConsoleClientChainManager;
import net.result.main.commands.ConsoleContext;
import net.result.main.commands.LoopCondition;
import net.result.main.commands.sandnode.*;
import net.result.main.commands.taulight.*;
import net.result.main.config.AgentPropertiesConfig;
import net.result.main.config.ClientPropertiesConfig;
import net.result.sandnode.exception.InvalidSandnodeLinkException;
import net.result.sandnode.exception.crypto.CreatingKeyException;
import net.result.sandnode.hubagent.AgentProtocol;
import net.result.sandnode.hubagent.ClientProtocol;
import net.result.sandnode.link.Links;
import net.result.sandnode.link.SandnodeLinkRecord;
import net.result.sandnode.serverclient.SandnodeClient;
import net.result.taulight.dto.ChatInfoDTO;
import net.result.taulight.dto.ChatMessageInputDTO;
import net.result.taulight.hubagent.TauAgent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class RunAgentWork implements Work {
    private static final Logger LOGGER = LogManager.getLogger(RunAgentWork.class);
    private SandnodeClient client;
    private ConsoleContext context = null;

    @Override
    public void run() throws Exception {
        Scanner scanner = new Scanner(System.in);

        SandnodeLinkRecord link;
        ClientPropertiesConfig clientConfig;
        while (true) {
            try {
                System.out.print("Enter link: ");
                link = Links.parse(scanner.nextLine());
                break;
            } catch (InvalidSandnodeLinkException | CreatingKeyException e) {
                System.out.println("Invalid link");
            }
        }

        clientConfig = new ClientPropertiesConfig();
        TauAgent agent = new TauAgent(new AgentPropertiesConfig());

        client = SandnodeClient.fromLink(link, agent, clientConfig);
        ConsoleClientChainManager chainManager = new ConsoleClientChainManager(client);

        // Starting client
        client.start(chainManager);

        // get key from fs or sending PUB if key not found
        final var serverKey = AgentProtocol.loadOrFetchServerKey(client, link);
        client.io().setServerKey(serverKey);

        // sending symmetric key
        ClientProtocol.sendSYM(client);

        // registration or login
        context = Auth.authenticateUser(scanner, client);
        processUserCommands();

        LOGGER.info("Exiting...");
        client.close();
    }

    private void sendChatMessage(String input, @NotNull ConsoleContext context) {
        if (context.currentChat == null) {
            System.out.println("chat not selected");
            return;
        }

        ChatMessageInputDTO message = new ChatMessageInputDTO()
                .setChatID(context.currentChat)
                .setNickname(client.nickname)
                .setSentDatetimeNow();

        try {
            UUID messageID = context.chain().sendMessage(context.chat, message, input, true, true);
            System.out.printf("Sent message uuid: %s %n", messageID);
        } catch (Exception e) {
            context.removeChain();
            throw new RuntimeException(e);
        }
    }

    private void processUserCommands() {
        Scanner scanner = new Scanner(System.in);
        Map<String, LoopCondition> commands = new HashMap<>();

        // Sandnode commands
        AuthCommands.register(commands);
        AvatarCommands.register(commands);
        ChainsCommands.register(commands);
        ClustersCommands.register(commands);
        DEKCommands.register(commands);
        UserInfoCommands.register(commands);

        // Taulight commands
        SettingsCommands.register(commands);
        ChatsCommands.register(commands);
        CodesCommands.register(commands);
        ReactionsCommands.register(commands);
        MessagesCommands.register(commands);
        RolesCommands.register(commands);
        GroupPermissionsCommands.register(commands);

        while (true) {
            ChatInfoDTO chat = context.chat;
            String result = chat != null ? switch (chat.chatType) {
                case DIALOG -> chat.otherNickname;
                case GROUP -> chat.title;
                case NOT_FOUND -> "NOT_FOUND";
            } : null;
            if ((result == null || result.isEmpty()) && context.currentChat != null) {
                result = context.currentChat.toString();
            }
            System.out.printf(" [%s] ", result == null ? "" : result);
            String input = scanner.nextLine();

            if (input.trim().isEmpty()) continue;

            String[] com_arg = input.split("\\s+");
            String command = com_arg[0];

            try {
                if (command.equals("exit")) {
                    context.io.disconnect(true);
                    break;
                } else if (commands.containsKey(command)) {
                    List<String> args = Arrays.stream(com_arg).skip(1).toList();
                    commands.get(command).run(args, context);
                } else {
                    sendChatMessage(input, context);
                }
            } catch (Exception e) {
                LOGGER.error("Unhandled exception", e);
            }
        }

        context.removeChain();
    }
}
