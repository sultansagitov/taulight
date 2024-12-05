package net.result.main;

import net.result.sandnode.exceptions.FSException;
import net.result.taulight.TauAgent;
import net.result.taulight.messages.*;
import net.result.taulight.messages.types.EchoMessage;
import net.result.taulight.messages.types.ForwardMessage;
import net.result.taulight.messages.types.TextMessage;
import net.result.sandnode.ClientProtocol;
import net.result.sandnode.AgentProtocol;
import net.result.sandnode.Agent;
import net.result.sandnode.client.SandnodeClient;
import net.result.sandnode.config.ClientPropertiesConfig;
import net.result.sandnode.config.IAgentConfig;
import net.result.sandnode.config.AgentPropertiesConfig;
import net.result.sandnode.exceptions.*;
import net.result.sandnode.exceptions.CannotUseEncryption;
import net.result.sandnode.exceptions.DecryptionException;
import net.result.sandnode.exceptions.EncryptionException;
import net.result.sandnode.exceptions.NoSuchEncryptionException;
import net.result.sandnode.link.HubInfo;
import net.result.sandnode.link.LinkInfo;
import net.result.sandnode.link.Links;
import net.result.sandnode.link.ServerLinkInfo;
import net.result.sandnode.messages.types.ExitMessage;
import net.result.sandnode.messages.IMessage;
import net.result.sandnode.messages.types.GroupMessage;
import net.result.sandnode.messages.types.RegistrationResponse;
import net.result.sandnode.messages.util.Headers;
import net.result.sandnode.messages.util.IMessageType;
import net.result.sandnode.util.Endpoint;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

import static net.result.sandnode.messages.util.MessageTypes.*;
import static net.result.sandnode.messages.util.NodeType.HUB;
import static net.result.sandnode.encryption.SymmetricEncryption.AES;

public class RunAgentWork implements IWork {
    private static final Logger LOGGER = LogManager.getLogger(RunAgentWork.class);

    private static void receiveThread(SandnodeClient client) {
        new Thread(() -> {
            try {
                while (client.io.isConnected()) {
                    IMessage response;
                    try {
                        response = client.io.receiveMessage();
                    } catch (UnexpectedSocketDisconnectException e) {
                        if (client.io.isConnected()) throw e;
                        else break;
                    }
                    if (response.getHeaders().getType() == EXIT) break;
                    onMessage(client, response);
                }
            } catch (DecryptionException | NoSuchEncryptionException | NoSuchMessageTypeException | KeyStorageNotFoundException |
                     UnexpectedSocketDisconnectException | EncryptionException | MessageSerializationException |
                     MessageWriteException e) {
                LOGGER.error("Error on receiving message thread", e);
            }
        }, "Receiving-thread").start();
    }

    private static void onMessage(@NotNull SandnodeClient client, @NotNull IMessage response)
            throws UnexpectedSocketDisconnectException, EncryptionException, KeyStorageNotFoundException,
            MessageSerializationException, MessageWriteException {
        IMessageType type = response.getHeaders().getType();

        if (type instanceof TauMessageTypes) {
            switch ((TauMessageTypes) type) {
                case ECHO, FWD -> LOGGER.info("From server: {}", new TextMessage(response).data);
                case ONL -> {
                    try {
                        LOGGER.info("Online agents: {}", new OnlineResponseMessage(response).members);
                    } catch (ExpectedMessageException e) {
                        throw new RuntimeException(e);
                    }
                }
                default -> client.ignoreMessage(response);
            }
        }
    }

    @Contract("_, _ -> new")
    private static @NotNull IMessage handle(@NotNull String input, @NotNull Headers headers) {
        if (input.equalsIgnoreCase("getonline")) {
            return new OnlineMessage(headers);

        } else if (input.startsWith("forward ")) {
            String data = input.substring(input.indexOf(" ") + 1);
            return new ForwardMessage(headers, data);

        } else if (input.startsWith("group ")) {
            String data = input.substring(input.indexOf(" ") + 1);
            Set<String> set = new HashSet<>(Arrays.asList(data.split(" ")));
            return new GroupMessage(headers, set);

        } else {
            return new EchoMessage(headers, input);
        }
    }

    @Override
    public void run() throws NoSuchEncryptionException, ConfigurationException, CannotUseEncryption,
            CreatingKeyException, InvalidLinkSyntaxException, URISyntaxException, UnexpectedSocketDisconnectException,
            EncryptionException, KeyStorageNotFoundException, DecryptionException, NoSuchMessageTypeException, KeyNotCreatedException,
            ExpectedMessageException, FSException, MessageSerializationException, MessageWriteException,
            ConnectionException, OutputStreamException, InputStreamException {
        Scanner scanner = new Scanner(System.in);

        ClientPropertiesConfig clientConfig = new ClientPropertiesConfig();
        IAgentConfig agentConfig = new AgentPropertiesConfig();

        System.out.print("link: ");

        LinkInfo linkInfo = Links.fromString(scanner.nextLine());

        if (!(linkInfo instanceof ServerLinkInfo link)) {
            throw new RuntimeException("Link is not for server");
        }

        if (link instanceof HubInfo hubLink) {
            Endpoint endpoint = hubLink.endpoint();

            Agent agent = new TauAgent(agentConfig);
            SandnodeClient client = new SandnodeClient(endpoint, agent, HUB, clientConfig);

            if (link.keyStorage() != null) {
                client.io.setMainKey(link.keyStorage());
            }
            client.setPublicKey();
            ClientProtocol.sendSYM(client);

            IMessage req = client.io.receiveMessage();
            ExpectedMessageException.check(req, REQ);

            System.out.print("[r for register or other for login] ");
            char c = scanner.nextLine().charAt(0);

            if (c == 'r') {
                System.out.print("memberID: ");
                String memberID = scanner.nextLine();
                System.out.print("password: ");
                String password = scanner.nextLine();

                RegistrationResponse response = AgentProtocol.registrationResponse(client, memberID, password);
                client.io.sendMessage(response);

                System.out.printf("Token for \"%s\"%n", memberID);
                System.out.println();
                System.out.println(response.getToken());
                System.out.println();
            }


            receiveThread(client);

            while (true) {
                System.out.printf("[%s] ", endpoint);
                String input = scanner.nextLine();

                try {
                    Headers headers = new Headers().set(AES).set(LOGIN);

                    if (input.equalsIgnoreCase("exit")) {
                        ExitMessage exitRequest = new ExitMessage(headers);
                        client.io.sendMessage(exitRequest, client.io.getServerMainEncryption());
                        client.io.disconnect();
                        break;
                    } else if (!input.isEmpty()) {
                        IMessage request = handle(input, headers);
                        client.io.sendMessage(request, client.io.getServerMainEncryption());
                    }
                } catch (EncryptionException | KeyStorageNotFoundException | UnexpectedSocketDisconnectException |
                         MessageWriteException | MessageSerializationException | SocketClosingException e) {
                    LOGGER.error("Error on console reading and sending cycle", e);
                    throw new RuntimeException(e);
                }
            }

            LOGGER.info("Exiting...");
            client.close();
            LOGGER.info("Closed");
        }

    }

}
