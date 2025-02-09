package net.result.main;

import net.result.main.chain.client.ConsoleForwardRequestClientChain;
import net.result.main.chain.ConsoleClientChainManager;
import net.result.main.config.ClientPropertiesConfig;
import net.result.sandnode.hubagent.AgentProtocol;
import net.result.sandnode.hubagent.ClientProtocol;
import net.result.sandnode.message.util.NodeType;
import net.result.sandnode.serverclient.ClientMember;
import net.result.sandnode.serverclient.SandnodeClient;
import net.result.sandnode.encryption.interfaces.*;
import net.result.sandnode.exception.*;
import net.result.sandnode.link.Links;
import net.result.sandnode.link.SandnodeLinkRecord;
import net.result.sandnode.message.types.ChainNameRequest;
import net.result.sandnode.util.EncryptionUtil;
import net.result.sandnode.util.Endpoint;
import net.result.sandnode.util.IOController;
import net.result.taulight.hubagent.TauAgent;
import net.result.taulight.chain.client.ChatClientChain;
import net.result.taulight.message.types.ChatRequest;
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

                clientConfig = new ClientPropertiesConfig();

                break;
            } catch (InvalidSandnodeLinkException | CreatingKeyException e) {
                System.out.println("Invalid link");
            } catch (SandnodeException e) {
                System.out.printf("%s: %s%n", e.getClass().getName(), e.getMessage());
            }
        }

        Endpoint endpoint = link.endpoint();
        TauAgent agent = new TauAgent();
        ConsoleClientChainManager chainManager = new ConsoleClientChainManager();

        SandnodeClient client = new SandnodeClient(endpoint, agent, NodeType.HUB, clientConfig);
        client.start(chainManager);                 // Starting client
        getPublicKey(client, agent, link);          // get key from fs or sending PUB if key not found
        ClientProtocol.sendSYM(client);             // sending symmetric key
        handleAuthentication(client.io, scanner);   // registration or login

        startTaulightChain(client.io);

        startConsoleChain(client.io);

        LOGGER.info("Exiting...");
        client.close();
    }

    private static void startTaulightChain(IOController io) throws InterruptedException {
        ChatClientChain chain = new ChatClientChain(io);
        io.chainManager.linkChain(chain);
        chain.send(new ChatRequest(ChatRequest.DataType.REMOVE));
        chain.send(new ChainNameRequest("tau"));
    }

    private static void startConsoleChain(IOController io)
            throws InterruptedException, ExpectedMessageException, DeserializationException {
        ConsoleForwardRequestClientChain consoleChain = new ConsoleForwardRequestClientChain(io);
        io.chainManager.linkChain(consoleChain);
        consoleChain.sync();
        io.chainManager.removeChain(consoleChain);
    }

    private static void getPublicKey(SandnodeClient client, TauAgent agent, SandnodeLinkRecord link)
            throws FSException, KeyAlreadySaved, LinkDoesNotMatchException, InterruptedException,
            EncryptionTypeException, NoSuchEncryptionException, CreatingKeyException, ExpectedMessageException,
            KeyStorageNotFoundException, DeserializationException {

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
        AsymmetricEncryption encryption = client.io.getServerEncryption().asymmetric();
        AsymmetricKeyStorage serverKey = agent.globalKeyStorage.getAsymmetricNonNull(encryption);

        client.clientConfig.saveKey(link.endpoint(), serverKey);
        client.io.setServerKey(serverKey);
    }

    private void handleAuthentication(IOController io, Scanner scanner)
            throws InterruptedException, ExpectedMessageException, DeserializationException {

        String s;
        do {
            System.out.print("[r for register, other for login]: ");
            s = scanner.nextLine();
        }
        while (s.isEmpty() || (s.charAt(0) != 'r' && s.charAt(0) != 'l'));

        char choice = s.charAt(0);

        switch (choice) {
            case 'r' -> {
                boolean isRegistered = false;
                while (!isRegistered) {
                    System.out.print("Member ID: ");
                    String memberID = scanner.nextLine();
                    System.out.print("Password: ");
                    String password = scanner.nextLine();

                    try {
                        String token = AgentProtocol.getTokenFromRegistration(io, memberID, password);
                        System.out.printf("Token for \"%s\":\n%s%n", memberID, token);
                        isRegistered = true;
                    } catch (BusyMemberIDException e) {
                        System.out.println("Member ID is busy");
                    } catch (InvalidMemberIDPassword e) {
                        System.out.println("Invalid Member ID or password");
                    }
                }
            }
            case 'l' -> {
                boolean isLoggedIn = false;

                while (!isLoggedIn) {
                    System.out.print("Token: ");
                    String token = scanner.nextLine();

                    if (token.isEmpty()) continue;

                    try {
                        ClientMember member = AgentProtocol.getMemberFromToken(io, token);
                        System.out.printf("You log in as %s%n", member.memberID);
                        isLoggedIn = true;
                    } catch (InvalidTokenException e) {
                        System.out.println("Invalid token. Please try again.");
                    } catch (ExpiredTokenException e) {
                        System.out.println("Expired token. Please try again.");
                    } catch (MemberNotFoundException e) {
                        System.out.println("Member not found. Please try again.");
                    }
                }
            }
            default -> throw new RuntimeException(String.valueOf(choice));
        }
    }
}
