package net.result.main;

import net.result.main.chain.ConsoleClientChainManager;
import net.result.main.commands.*;
import net.result.main.config.AgentPropertiesConfig;
import net.result.main.config.ClientPropertiesConfig;
import net.result.sandnode.config.KeyEntry;
import net.result.sandnode.dto.LogPasswdResponseDTO;
import net.result.sandnode.dto.LoginResponseDTO;
import net.result.sandnode.encryption.AsymmetricEncryptions;
import net.result.sandnode.encryption.interfaces.AsymmetricKeyStorage;
import net.result.sandnode.exception.*;
import net.result.sandnode.exception.crypto.CreatingKeyException;
import net.result.sandnode.exception.error.*;
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
    private Scanner scanner;
    private ConsoleContext context = null;

    @Override
    public void run() throws Exception {
        scanner = new Scanner(System.in);

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
        authenticateUser();
        processUserCommands();

        LOGGER.info("Exiting...");
        client.close();
    }

    private void authenticateUser() throws Exception {
        while (context == null) {
            String s;
            do {
                System.out.print("[r for register, 'l' for login by password, 't' for login by token]: ");
                s = scanner.nextLine();
            }
            while (s.isEmpty() || (s.charAt(0) != 'r' && s.charAt(0) != 't' && s.charAt(0) != 'l'));

            char choice = s.charAt(0);

            switch (choice) {
                case 'r' -> register();
                case 't' -> login();
                case 'l' -> password();
            }
        }
    }

    private void register() throws Exception {
        System.out.print("Nickname: ");
        String nickname = scanner.nextLine();
        System.out.print("Password: ");
        String password = scanner.nextLine();
        System.out.print("Device: ");
        String device = scanner.nextLine();

        AsymmetricKeyStorage keyStorage = AsymmetricEncryptions.ECIES.generate();

        try {
            var result = AgentProtocol.register(client, nickname, password, device, keyStorage);
            System.out.printf("Token for \"%s\":%n%s%n", nickname, result.token);
            context = new ConsoleContext(client, result.keyID);
        } catch (BusyNicknameException e) {
            System.out.println("Nickname is busy");
        } catch (InvalidNicknamePassword e) {
            System.out.println("Invalid nickname or password");
        } catch (SandnodeErrorException | UnknownSandnodeErrorException e) {
            System.out.printf("Unknown sandnode error exception. Please try again. %s%n", e.getClass());
        }
    }

    private void login() throws Exception {
        System.out.print("Token: ");
        String token = scanner.nextLine();

        if (token.isEmpty()) return;

        try {
            LoginResponseDTO result = AgentProtocol.byToken(client, token);
            System.out.printf("You log in as %s%n", result.nickname);
            context = new ConsoleContext(client, result.keyID);
        } catch (InvalidArgumentException e) {
            System.out.println("Invalid token. Please try again.");
        } catch (ExpiredTokenException e) {
            System.out.println("Expired token. Please try again.");
        } catch (SandnodeErrorException | UnknownSandnodeErrorException e) {
            System.out.printf("Unknown sandnode error exception. Please try again. %s%n", e.getClass());
        }
    }

    private void password() throws Exception {
        System.out.print("Nickname: ");
        String nickname = scanner.nextLine();
        System.out.print("Password: ");
        String password = scanner.nextLine();
        System.out.print("Device: ");
        String device = scanner.nextLine();

        if (nickname.isEmpty() || password.isEmpty()) return;

        try {
            LogPasswdResponseDTO result = AgentProtocol.byPassword(client, nickname, password, device);
            System.out.printf("Token for \"%s\": %n%s%n", nickname, result.token);
            context = new ConsoleContext(client, result.keyID);
        } catch (UnauthorizedException e) {
            System.out.println("Incorrect nickname or password. Please try again.");
        } catch (SandnodeErrorException | UnknownSandnodeErrorException e) {
            System.out.printf("Unknown sandnode error exception. Please try again. %s%n", e.getClass());
        }
    }

    private void sendChatMessage(String input, @NotNull ConsoleContext context) throws Exception {
        if (context.currentChat == null) {
            System.out.println("chat not selected");
            return;
        }

        ChatMessageInputDTO message = new ChatMessageInputDTO()
                .setChatID(context.currentChat)
                .setNickname(client.nickname)
                .setSentDatetimeNow();

        if (context.chat.chatType == ChatInfoDTO.ChatType.DIALOG) {
            try {
                String otherNickname = context.chat.otherNickname;
                KeyEntry dek = client.node().agent().config.loadDEK(client.address, otherNickname);

                LOGGER.debug("Using {} {}", dek.id(), dek.keyStorage());

                message.setEncryptedContent(dek.id(), dek.keyStorage(), input);
            } catch (KeyStorageNotFoundException e) {
                LOGGER.error("Using null", e);
                message.setContent(input);
            }
        } else {
            message.setContent(input);
        }


        try {
            UUID messageID = context.chain().message(message);
            System.out.printf("Sent message uuid: %s %n", messageID);
        } catch (DeserializationException e) {
            System.out.println("Sent message with unknown uuid due deserialization");
            context.io.chainManager.removeChain(context.chain);
            context.chain = null;
        } catch (NotFoundException e) {
            System.out.printf("Chat %s was not found%n", context.currentChat);
            context.io.chainManager.removeChain(context.chain);
            context.chain = null;
        } catch (NoEffectException e) {
            System.out.println("Message not forwarded");
            context.io.chainManager.removeChain(context.chain);
            context.chain = null;
        } catch (UnknownSandnodeErrorException | SandnodeErrorException e) {
            LOGGER.error("Error", e);
            context.io.chainManager.removeChain(context.chain);
            context.chain = null;
        }
    }

    private void processUserCommands() {
        Scanner scanner = new Scanner(System.in);
        Map<String, LoopCondition> commands = new HashMap<>();
        ConsoleSandnodeCommands.register(commands);
        ConsoleSettingsCommands.register(commands);
        ConsoleChatsCommands.register(commands);
        ConsoleCodesCommands.register(commands);
        ConsoleReactionsCommands.register(commands);
        ConsoleMessagesCommands.register(commands);
        ConsoleRolesCommands.register(commands);
        ConsoleGroupPermissionsCommands.register(commands);

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
            } catch (UnprocessedMessagesException e) {
                System.out.println("Unprocessed message: Sent before handling received: " + e.raw);
            } catch (ExpectedMessageException e) {
                System.out.printf("Unexpected message type. Expected: %s, Message: %s%n", e.expectedType, e.message);
            } catch (Exception e) {
                LOGGER.error("Unhandled exception", e);
            }
        }

        if (context.chain != null) {
            client.io().chainManager.removeChain(context.chain);
            context.chain = null;
        }
    }
}
