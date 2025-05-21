package net.result.main;

import net.result.main.chain.ConsoleClientChainManager;
import net.result.main.chain.sender.ConsoleForwardRequestClientChain;
import net.result.main.commands.*;
import net.result.main.config.ClientPropertiesConfig;
import net.result.sandnode.dto.LogPasswdResponseDTO;
import net.result.sandnode.dto.LoginResponseDTO;
import net.result.sandnode.encryption.AsymmetricEncryptions;
import net.result.sandnode.encryption.interfaces.AsymmetricEncryption;
import net.result.sandnode.encryption.interfaces.AsymmetricKeyStorage;
import net.result.sandnode.encryption.interfaces.KeyStorage;
import net.result.sandnode.exception.*;
import net.result.sandnode.exception.crypto.CreatingKeyException;
import net.result.sandnode.exception.crypto.CryptoException;
import net.result.sandnode.exception.error.*;
import net.result.sandnode.hubagent.AgentProtocol;
import net.result.sandnode.hubagent.ClientProtocol;
import net.result.sandnode.link.Links;
import net.result.sandnode.link.SandnodeLinkRecord;
import net.result.sandnode.serverclient.SandnodeClient;
import net.result.sandnode.util.EncryptionUtil;
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
        TauAgent agent = new TauAgent();

        client = SandnodeClient.fromLink(link, agent, clientConfig);
        ConsoleClientChainManager chainManager = new ConsoleClientChainManager(client);

        client.start(chainManager);                         // Starting client
        client.io.setServerKey(loadServerPublicKey(link));  // get key from fs or sending PUB if key not found
        ClientProtocol.sendSYM(client);                     // sending symmetric key
        authenticateUser();                                 // registration or login
        processUserCommands();

        LOGGER.info("Exiting...");
        client.close();
    }

    private AsymmetricKeyStorage loadServerPublicKey(@NotNull SandnodeLinkRecord link)
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
                return fileKey;
            } else {
                client.clientConfig.saveKey(link.endpoint(), linkKeyStorage);
                return linkKeyStorage;
            }
        } else if (filePublicKey.isPresent()) {
            return filePublicKey.get();
        } else {
            ClientProtocol.PUB(client);
            AsymmetricEncryption encryption = client.io.serverEncryption().asymmetric();
            AsymmetricKeyStorage serverKey = agent.keyStorageRegistry.asymmetricNonNull(encryption);

            client.clientConfig.saveKey(link.endpoint(), serverKey);
            return serverKey;
        }
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

            client.clientConfig.saveMemberKey(result.keyID, keyStorage);
            context = new ConsoleContext(client, nickname, result.keyID);
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
            context = new ConsoleContext(client, result.nickname, result.keyID);
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
            context = new ConsoleContext(client, nickname, result.keyID);
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

        // TODO replace with key of other member
        UUID keyID = context.keyID;
        KeyStorage keyStorage = client.clientConfig.loadMemberKey(keyID).get();

        ChatMessageInputDTO message = new ChatMessageInputDTO()
                .setChatID(context.currentChat)
                .setEncryptedContent(keyID, keyStorage, input)
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

    private void processUserCommands() {
        Scanner scanner = new Scanner(System.in);
        Map<String, ConsoleSandnodeCommands.LoopCondition> commands = new HashMap<>();
        ConsoleSandnodeCommands.register(commands);
        ConsoleChatsCommands.register(commands);
        ConsoleCodesCommands.register(commands);
        ConsoleReactionsCommands.register(commands);
        ConsoleMessagesCommands.register(commands);
        ConsoleRolesCommands.register(commands);

        ConsoleForwardRequestClientChain chain = new ConsoleForwardRequestClientChain(client);
        client.io.chainManager.linkChain(chain);

        context.chain = chain;

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
