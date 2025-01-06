package net.result.main;

import net.result.main.chains.ConsoleClientChain;
import net.result.main.chains.ConsoleClientChainManager;
import net.result.main.config.ClientPropertiesConfig;
import net.result.sandnode.*;
import net.result.sandnode.client.ClientMember;
import net.result.sandnode.client.SandnodeClient;
import net.result.sandnode.encryption.interfaces.*;
import net.result.sandnode.exceptions.*;
import net.result.sandnode.link.Links;
import net.result.sandnode.link.SandnodeLinkRecord;
import net.result.sandnode.util.EncryptionUtil;
import net.result.sandnode.util.Endpoint;
import net.result.sandnode.util.IOControl;
import net.result.taulight.TauAgent;
import net.result.taulight.chain.ForwardClientChain;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static net.result.sandnode.messages.util.NodeType.HUB;

public class RunAgentWork implements IWork {
    private static final Logger LOGGER = LogManager.getLogger(RunAgentWork.class);

    @Override
    public void run() throws InterruptedException, SandnodeException {
        Scanner scanner = new Scanner(System.in);

        System.out.print("Enter link: ");
        SandnodeLinkRecord link = Links.parse(scanner.nextLine());

        Endpoint endpoint = link.endpoint();
        TauAgent agent = new TauAgent();
        ClientPropertiesConfig clientConfig = new ClientPropertiesConfig();
        SandnodeClient client = new SandnodeClient(
                endpoint,
                agent,
                HUB,
                clientConfig
        );

        client.start(ConsoleClientChainManager::new);

        getPublicKey(client, agent, link);

        ClientProtocol.sendSYM(client);

        handleAuthentication(client.io, scanner);

        ExecutorService executorService = Executors.newCachedThreadPool();

        startForwardChain(executorService, client.io);

        startConsoleChain(client.io);

        LOGGER.info("Exiting...");
        executorService.shutdownNow();
        client.close();
    }

    private static void startConsoleChain(IOControl io) throws InterruptedException, ExpectedMessageException, DeserializationException {
        ConsoleClientChain consoleChain = new ConsoleClientChain(io);
        io.chainManager.linkChain(consoleChain);
        consoleChain.sync();
        io.chainManager.removeChain(consoleChain);
    }

    private static void startForwardChain(ExecutorService executorService, IOControl io) {
        ForwardClientChain fwd = new ForwardClientChain(io);
        io.chainManager.linkChain(fwd);
        fwd.async(executorService);
    }

    private static void getPublicKey(SandnodeClient client, TauAgent agent, SandnodeLinkRecord link) throws FSException,
            KeyAlreadySaved, LinkDoesNotMatchException, InterruptedException, EncryptionTypeException,
            NoSuchEncryptionException, CreatingKeyException, ExpectedMessageException, KeyStorageNotFoundException {
        Optional<IAsymmetricKeyStorage> filePublicKey = client.clientConfig.getPublicKey(link.endpoint());
        if (link.keyStorage() != null) {
            IAsymmetricKeyStorage linkKey = link.keyStorage();

            if (filePublicKey.isEmpty()) {
                client.clientConfig.saveKey(link.endpoint(), linkKey);
                client.io.setServerKey(linkKey);
            } else if (EncryptionUtil.isPublicKeysEquals(filePublicKey.get(), linkKey)) {
                LOGGER.info("Key already saved and matches");
                client.io.setServerKey(filePublicKey.get());
            } else {
                throw new LinkDoesNotMatchException("Key mismatch with saved configuration");
            }
        } else if (filePublicKey.isPresent()) {
            client.io.setServerKey(filePublicKey.get());
        } else {
            ClientProtocol.PUB(client.io);
            IAsymmetricEncryption encryption = client.io.getServerEncryption().asymmetric();
            IAsymmetricKeyStorage serverKey = agent.globalKeyStorage.getAsymmetricNonNull(encryption);

            client.clientConfig.saveKey(link.endpoint(), serverKey);
            client.io.setServerKey(serverKey);
        }
    }

    private void handleAuthentication(IOControl io, Scanner scanner) throws InterruptedException,
            ExpectedMessageException, MemberNotFoundException, BusyMemberIDException, DeserializationException {
        System.out.print("[r for register, other for login]: ");
        String s = scanner.nextLine();
        char choice = s.isEmpty() ? 'r' : s.charAt(0);

        switch (choice) {
            case 'r' -> {
                System.out.print("Member ID: "); String memberID = scanner.nextLine();
                System.out.print("Password: ");  String password = scanner.nextLine();

                String token = AgentProtocol.getTokenFromRegistration(io, memberID, password);
                LOGGER.info("Token for \"{}\":\n{}", memberID, token);
            }
            case 'l' -> {
                System.out.print("Token: ");
                String token = scanner.nextLine();

                ClientMember member = AgentProtocol.getMemberFromToken(io, token);

                System.out.printf("You log in as %s%n", member.memberID());
            }
            default -> throw new RuntimeException(String.valueOf(choice));
        }
    }
}
