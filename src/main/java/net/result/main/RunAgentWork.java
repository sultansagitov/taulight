package net.result.main;

import net.result.main.chain.ConsoleClientChainManager;
import net.result.main.chain.sender.ConsoleForwardRequestClientChain;
import net.result.main.commands.*;
import net.result.main.config.ClientPropertiesConfig;
import net.result.sandnode.encryption.AsymmetricEncryptions;
import net.result.sandnode.exception.*;
import net.result.sandnode.exception.crypto.CreatingKeyException;
import net.result.sandnode.exception.crypto.CryptoException;
import net.result.sandnode.exception.error.*;
import net.result.sandnode.hubagent.AgentProtocol;
import net.result.sandnode.hubagent.ClientProtocol;
import net.result.sandnode.serverclient.SandnodeClient;
import net.result.sandnode.encryption.interfaces.*;
import net.result.sandnode.link.Links;
import net.result.sandnode.link.SandnodeLinkRecord;
import net.result.sandnode.util.EncryptionUtil;
import net.result.sandnode.util.IOController;
import net.result.taulight.dto.ChatMessageInputDTO;
import net.result.taulight.hubagent.TauAgent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class RunAgentWork implements IWork {
    private static final Logger LOGGER = LogManager.getLogger(RunAgentWork.class);
    private SandnodeClient client;
    private Scanner scanner;

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
        TauAgent agent = new TauAgent();
        ConsoleClientChainManager chainManager = new ConsoleClientChainManager();

        client = SandnodeClient.fromLink(link, agent, clientConfig);
        client.start(chainManager);             // Starting client
        ensureServerPublicKey(link);            // get key from fs or sending PUB if key not found
        ClientProtocol.sendSYM(client);         // sending symmetric key
        String nickname = authenticateUser();   // registration or login
        processUserCommands(nickname, client.io);

        LOGGER.info("Exiting...");
        client.close();
    }

    private void ensureServerPublicKey(@NotNull SandnodeLinkRecord link)
            throws FSException, CryptoException, LinkDoesNotMatchException, InterruptedException,
            SandnodeErrorException, ExpectedMessageException, UnknownSandnodeErrorException,
            UnprocessedMessagesException {

        TauAgent agent = (TauAgent) client.node;

        Optional<AsymmetricKeyStorage> filePublicKey = client.clientConfig.getPublicKey(link.endpoint());
        AsymmetricKeyStorage linkKeyStorage = link.keyStorage();

        if (linkKeyStorage != null) {
            if (filePublicKey.isPresent()) {
                AsymmetricKeyStorage fileKey = filePublicKey.get();

                if (!EncryptionUtil.isPublicKeysEquals(fileKey, linkKeyStorage))
                    throw new LinkDoesNotMatchException("Key mismatch with saved configuration");

                LOGGER.info("Key already saved and matches");
                client.io.setServerKey(fileKey);
                return;
            }

            client.clientConfig.saveKey(link.endpoint(), linkKeyStorage);
            client.io.setServerKey(linkKeyStorage);
            return;
        }

        if (filePublicKey.isPresent()) {
            client.io.setServerKey(filePublicKey.get());
            return;
        }

        ClientProtocol.PUB(client.io);
        AsymmetricEncryption encryption = client.io.serverEncryption().asymmetric();
        AsymmetricKeyStorage serverKey = agent.keyStorageRegistry.asymmetricNonNull(encryption);

        client.clientConfig.saveKey(link.endpoint(), serverKey);
        client.io.setServerKey(serverKey);
    }

    private String authenticateUser() throws Exception {
        String s;
        do {
            System.out.print("[r for register, 'l' for login by password, 't' for login by token]: ");
            s = scanner.nextLine();
        }
        while (s.isEmpty() || (s.charAt(0) != 'r' && s.charAt(0) != 't' && s.charAt(0) != 'l'));

        char choice = s.charAt(0);

        return switch (choice) {
            case 'r' -> register();
            case 't' -> login();
            case 'l' -> password();
            default -> throw new RuntimeException(String.valueOf(choice));
        };
    }

    private @NotNull String register() throws Exception {
        boolean isRegistered = false;
        String nickname = "";
        while (!isRegistered) {
            System.out.print("Nickname: ");
            nickname = scanner.nextLine();
            System.out.print("Password: ");
            String password = scanner.nextLine();
            System.out.print("Device: ");
            String device = scanner.nextLine();

            AsymmetricKeyStorage keyStorage = AsymmetricEncryptions.ECIES.generate();

            try {
                var result = AgentProtocol.register(client.io, nickname, password, device, keyStorage);
                System.out.printf("Token for \"%s\":%n%s%n", nickname, result.token);

                client.clientConfig.saveMemberKey(result.keyID, keyStorage);

                isRegistered = true;
            } catch (BusyNicknameException e) {
                System.out.println("Nickname is busy");
            } catch (InvalidNicknamePassword e) {
                System.out.println("Invalid nickname or password");
            } catch (SandnodeErrorException | UnknownSandnodeErrorException e) {
                System.out.printf("Unknown sandnode error exception. Please try again. %s%n", e.getClass());
            }
        }
        return nickname;
    }

    private String login() throws Exception {
        boolean isLoggedIn = false;
        String nickname = "";
        while (!isLoggedIn) {
            System.out.print("Token: ");
            String token = scanner.nextLine();

            if (token.isEmpty()) continue;

            try {
                nickname = AgentProtocol.getMemberFromToken(client.io, token);
                System.out.printf("You log in as %s%n", nickname);
                isLoggedIn = true;
            } catch (InvalidArgumentException e) {
                System.out.println("Invalid token. Please try again.");
            } catch (ExpiredTokenException e) {
                System.out.println("Expired token. Please try again.");
            } catch (SandnodeErrorException | UnknownSandnodeErrorException e) {
                System.out.printf("Unknown sandnode error exception. Please try again. %s%n", e.getClass());
            }
        }
        return nickname;
    }

    private @NotNull String password() throws Exception {
        boolean isLoggedIn = false;
        String nickname = "";
        while (!isLoggedIn) {
            System.out.print("Nickname: ");
            nickname = scanner.nextLine();
            System.out.print("Password: ");
            String password = scanner.nextLine();
            System.out.print("Device: ");
            String device = scanner.nextLine();

            if (nickname.isEmpty() || password.isEmpty()) continue;

            try {
                String token = AgentProtocol.getTokenByNicknameAndPassword(client.io, nickname, password, device);
                System.out.printf("Token for \"%s\": %n%s%n", nickname, token);
                isLoggedIn = true;
            } catch (UnauthorizedException e) {
                System.out.println("Incorrect nickname or password. Please try again.");
            } catch (SandnodeErrorException | UnknownSandnodeErrorException e) {
                System.out.printf("Unknown sandnode error exception. Please try again. %s%n", e.getClass());
            }
        }
        return nickname;
    }

    private void sendChatMessage(String input, @NotNull ConsoleContext context) throws Exception {
        if (context.currentChat == null) {
            System.out.println("chat not selected");
            return;
        }

        ChatMessageInputDTO message = new ChatMessageInputDTO()
                .setChatID(context.currentChat)
                .setContent(input)
                .setNickname(context.nickname)
                .setSentDatetimeNow();

        try {
            UUID messageID = context.chain.message(message);
            System.out.printf("Sent message uuid: %s %n", messageID);
        } catch (DeserializationException e) {
            System.out.println("Sent message with unknown uuid due deserialization");
        } catch (NotFoundException e) {
            System.out.printf("Chat %s was not found%n", context.currentChat);
        } catch (NoEffectException e) {
            System.out.println("Message not forwarded");
        } catch (UnknownSandnodeErrorException | SandnodeErrorException e) {
            System.out.printf("%s: %s%n", e.getClass().getSimpleName(), e.getMessage());
        }
    }

    private void processUserCommands(String nickname, IOController io) {
        Scanner scanner = new Scanner(System.in);
        Map<String, ConsoleSandnodeCommands.LoopCondition> commands = new HashMap<>();
        ConsoleSandnodeCommands.register(commands);
        ConsoleChatsCommands.register(commands);
        ConsoleCodesCommands.register(commands);
        ConsoleReactionsCommands.register(commands);
        ConsoleMessagesCommands.register(commands);
        ConsoleRolesCommands.register(commands);

        ConsoleForwardRequestClientChain chain = new ConsoleForwardRequestClientChain(client.io);
        client.io.chainManager.linkChain(chain);

        ConsoleContext context = new ConsoleContext(chain, io, nickname);

        while (true) {
            System.out.printf(" [%s] ", Optional.ofNullable(context.currentChat).map(UUID::toString).orElse(""));
            String input = scanner.nextLine();

            if (input.trim().isEmpty()) continue;

            String[] com_arg = input.split("\\s+");
            String command = com_arg[0];

            try {
                if (commands.containsKey(command)) {
                    List<String> args = Arrays.stream(com_arg).skip(1).toList();
                    if (commands.get(command).breakLoop(args, context)) break;
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

        client.io.chainManager.removeChain(chain);
    }
}
