package net.result.main;

import net.result.main.chain.sender.ConsoleForwardRequestClientChain;
import net.result.main.chain.ConsoleClientChainManager;
import net.result.main.config.ClientPropertiesConfig;
import net.result.sandnode.exception.crypto.*;
import net.result.sandnode.exception.error.*;
import net.result.sandnode.hubagent.AgentProtocol;
import net.result.sandnode.hubagent.ClientProtocol;
import net.result.sandnode.serverclient.SandnodeClient;
import net.result.sandnode.encryption.interfaces.*;
import net.result.sandnode.exception.*;
import net.result.sandnode.link.Links;
import net.result.sandnode.link.SandnodeLinkRecord;
import net.result.sandnode.util.EncryptionUtil;
import net.result.sandnode.util.IOController;
import net.result.taulight.hubagent.TauAgent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Optional;
import java.util.Scanner;

public class RunAgentWork implements IWork {
    private static final Logger LOGGER = LogManager.getLogger(RunAgentWork.class);

    @Override
    public void run() throws InterruptedException, SandnodeException {
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
        TauAgent agent = new TauAgent();
        ConsoleClientChainManager chainManager = new ConsoleClientChainManager();

        SandnodeClient client = SandnodeClient.fromLink(link, agent, clientConfig);
        client.start(chainManager);                                 // Starting client
        getPublicKey(client, agent, link);                          // get key from fs or sending PUB if key not found
        ClientProtocol.sendSYM(client);                             // sending symmetric key
        String nickname = handleAuthentication(client.io, scanner); // registration or login
        startConsoleChain(client.io, nickname);

        LOGGER.info("Exiting...");
        client.close();
    }

    private static void startConsoleChain(IOController io, String nickname) {
        ConsoleForwardRequestClientChain consoleChain = new ConsoleForwardRequestClientChain(io);
        io.chainManager.linkChain(consoleChain);
        consoleChain.sync(nickname);
        io.chainManager.removeChain(consoleChain);
    }

    private static void getPublicKey(SandnodeClient client, TauAgent agent, SandnodeLinkRecord link)
            throws FSException, CryptoException, LinkDoesNotMatchException, InterruptedException,
            SandnodeErrorException, ExpectedMessageException, UnknownSandnodeErrorException,
            UnprocessedMessagesException {

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

    private String handleAuthentication(IOController io, Scanner scanner) throws InterruptedException,
            ExpectedMessageException, DeserializationException, UnprocessedMessagesException {

        String s;
        do {
            System.out.print("[r for register, 'l' for login by password, 't' for login by token]: ");
            s = scanner.nextLine();
        }
        while (s.isEmpty() || (s.charAt(0) != 'r' && s.charAt(0) != 't' && s.charAt(0) != 'l'));

        char choice = s.charAt(0);

        String nickname = "";
        switch (choice) {
            case 'r' -> {
                boolean isRegistered = false;
                while (!isRegistered) {
                    System.out.print("Nickname: ");
                    nickname = scanner.nextLine();
                    System.out.print("Password: ");
                    String password = scanner.nextLine();

                    try {
                        String token = AgentProtocol.getTokenFromRegistration(io, nickname, password);
                        System.out.printf("Token for \"%s\":%n%s%n", nickname, token);
                        isRegistered = true;
                    } catch (BusyNicknameException e) {
                        System.out.println("Nickname is busy");
                    } catch (InvalidNicknamePassword e) {
                        System.out.println("Invalid nickname or password");
                    } catch (SandnodeErrorException | UnknownSandnodeErrorException e) {
                        System.out.printf("Unknown sandnode error exception. Please try again. %s%n", e.getClass());
                    }
                }
            }
            case 't' -> {
                boolean isLoggedIn = false;

                while (!isLoggedIn) {
                    System.out.print("Token: ");
                    String token = scanner.nextLine();

                    if (token.isEmpty()) continue;

                    try {
                        nickname = AgentProtocol.getMemberFromToken(io, token);
                        System.out.printf("You log in as %s%n", nickname);
                        isLoggedIn = true;
                    } catch (InvalidTokenException e) {
                        System.out.println("Invalid token. Please try again.");
                    } catch (ExpiredTokenException e) {
                        System.out.println("Expired token. Please try again.");
                    } catch (SandnodeErrorException | UnknownSandnodeErrorException e) {
                        System.out.printf("Unknown sandnode error exception. Please try again. %s%n", e.getClass());
                    }
                }
            }
            case 'l' -> {
                boolean isLoggedIn = false;

                while (!isLoggedIn) {
                    System.out.print("Nickname: ");
                    nickname = scanner.nextLine();
                    System.out.print("Password: ");
                    String password = scanner.nextLine();

                    if (nickname.isEmpty() || password.isEmpty()) continue;

                    try {
                        String token = AgentProtocol.getTokenByNicknameAndPassword(io, nickname, password);
                        System.out.printf("Token for \"%s\": %n%s%n", nickname, token);
                        isLoggedIn = true;
                    } catch (UnauthorizedException e) {
                        System.out.println("Incorrect nickname or password. Please try again.");
                    } catch (SandnodeErrorException | UnknownSandnodeErrorException e) {
                        System.out.printf("Unknown sandnode error exception. Please try again. %s%n", e.getClass());
                    }
                }
            }
            default -> throw new RuntimeException(String.valueOf(choice));
        }
        return nickname;
    }
}
