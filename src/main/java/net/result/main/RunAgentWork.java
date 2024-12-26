package net.result.main;

import net.result.main.chains.ConsoleClientChain;
import net.result.main.chains.ConsoleClientChainManager;
import net.result.main.config.ClientPropertiesConfig;
import net.result.sandnode.*;
import net.result.sandnode.chain.IChain;
import net.result.sandnode.client.SandnodeClient;
import net.result.sandnode.encryption.interfaces.*;
import net.result.sandnode.exceptions.*;
import net.result.sandnode.link.Links;
import net.result.sandnode.link.SandnodeLinkRecord;
import net.result.sandnode.util.EncryptionUtil;
import net.result.sandnode.util.Endpoint;
import net.result.taulight.TauAgent;
import net.result.taulight.chain.ForwardClientChain;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

import static net.result.sandnode.messages.util.NodeType.HUB;

public class RunAgentWork implements IWork {
    private static final Logger LOGGER = LogManager.getLogger(RunAgentWork.class);

    private void handleAuthentication(SandnodeClient client, Scanner scanner) throws InterruptedException,
            ExpectedMessageException, EncryptionTypeException, MemberNotFound, NoSuchEncryptionException,
            CreatingKeyException, KeyNotCreatedException, KeyStorageNotFoundException, DataNotEncryptedException {
        System.out.print("[r for register, other for login]: ");
        String s = scanner.nextLine();
        char choice = s.isEmpty() ? 'r' : s.charAt(0);

        switch (choice) {
            case 'r' -> {
                System.out.print("Member ID: "); String memberID = scanner.nextLine();
                System.out.print("Password: ");  String password = scanner.nextLine();

                String token = AgentProtocol.getTokenFromRegistration(client, memberID, password);
                LOGGER.info("Token for \"{}\":\n{}", memberID, token);
            }
            case 'l' -> {
                System.out.print("Token: ");
                String token = scanner.nextLine();

//                String token = AgentProtocol.getMemberFromToken(client, token);
            }
            default -> throw new RuntimeException(String.valueOf(choice));
        }
    }

    @Override
    public void run() throws SandnodeException, InterruptedException {
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
                clientConfig,
                ConsoleClientChainManager::new
        );

        Optional<IAsymmetricKeyStorage> filePublicKey = client.clientConfig.getPublicKey(client.endpoint);
        if (link.keyStorage() != null) {
            IAsymmetricKeyStorage linkKey = link.keyStorage();

            if (filePublicKey.isEmpty()) {
                client.clientConfig.saveKey(client.endpoint, linkKey);
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
            IAsymmetricKeyStorage serverKey = client.node.globalKeyStorage.getAsymmetricNonNull(encryption);

            client.clientConfig.saveKey(client.endpoint, serverKey);
            client.io.setServerKey(serverKey);
        }

        ClientProtocol.sendSYM(client);

        handleAuthentication(client, scanner);

        IChain fwd = new ForwardClientChain(client.io);
        client.io.chainManager.addChain(fwd);
        fwd.async();

        IChain consoleChain = new ConsoleClientChain(client.io);
        client.io.chainManager.addChain(consoleChain);
        consoleChain.sync();
        LOGGER.info("Exiting...");
        client.close();
    }
}
