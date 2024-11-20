package net.result.main;

import net.result.openhelo.HeloUser;
import net.result.openhelo.messages.*;
import net.result.sandnode.User;
import net.result.sandnode.client.Client;
import net.result.sandnode.config.IUserConfig;
import net.result.sandnode.config.UserPropertiesConfig;
import net.result.sandnode.exceptions.*;
import net.result.sandnode.exceptions.encryption.CannotUseEncryption;
import net.result.sandnode.exceptions.encryption.DecryptionException;
import net.result.sandnode.exceptions.encryption.EncryptionException;
import net.result.sandnode.exceptions.encryption.NoSuchEncryptionException;
import net.result.sandnode.link.HubInfo;
import net.result.sandnode.link.Links;
import net.result.sandnode.link.ServerLinkInfo;
import net.result.sandnode.messages.ExitMessage;
import net.result.sandnode.messages.IMessage;
import net.result.sandnode.messages.util.HeadersBuilder;
import net.result.sandnode.util.Endpoint;
import net.result.sandnode.util.encryption.interfaces.IAsymmetricKeyStorage;
import net.result.sandnode.util.encryption.interfaces.IKeyStorage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Objects;
import java.util.Scanner;

import static net.result.sandnode.messages.util.MessageTypes.MSG;
import static net.result.sandnode.messages.util.NodeType.HUB;
import static net.result.sandnode.util.encryption.SymmetricEncryption.AES;

public class RunUserWork implements IWork {
    private static final Logger LOGGER = LogManager.getLogger(RunUserWork.class);

    private static @NotNull Thread getReceiveThread(Client client) {
        Thread receiveThread = new Thread(() -> {
            try {
                while (client.isConnected()) {
                    IMessage response = client.receiveMessage();
                    if (response == null) break;
                    onMessage(response);
                }
            } catch (ReadingKeyException | DecryptionException | NoSuchEncryptionException | NoSuchReqHandler |
                     KeyStorageNotFoundException | UnexpectedSocketDisconnect e) {
                LOGGER.error("Error on receiving message thread", e);
            }
        });
        receiveThread.setName("Receiving-thread");
        return receiveThread;
    }

    private static void onMessage(@NotNull IMessage response) {
        switch ((HeloMessageTypes) response.getHeaders().getType()) {
            case ECH, FWD -> LOGGER.info("From server: {}", new TextMessage(response).data);
            case ONL_RES -> LOGGER.info("Online users: {}", new OnlineResponseMessage(response).users);
        }
    }

    @Contract("_, _ -> new")
    private static @NotNull IMessage handle(@NotNull String input, @NotNull HeadersBuilder headersBuilder) {
        if (input.equalsIgnoreCase("getonline")) {
            return new OnlineMessage(headersBuilder);

        } else if (input.startsWith("forward ")) {
            String data = input.substring(input.indexOf(" ") + 1);
            return new ForwardMessage(headersBuilder, data);

        } else {
            return new EchoMessage(headersBuilder, input);
        }
    }

    @Override
    public void run() throws IOException, ReadingKeyException, NoSuchEncryptionException, DecryptionException,
            EncryptionException, NoSuchReqHandler, CreatingKeyException, InvalidLinkSyntax, ConfigurationException,
            CannotUseEncryption, UnexpectedSocketDisconnect, KeyStorageNotFoundException, KeyNotCreated, URISyntaxException {
        Scanner scanner = new Scanner(System.in);
        IUserConfig userConfig = new UserPropertiesConfig();

        System.out.print("link: ");
        String domainString = scanner.nextLine();
        ServerLinkInfo link = (ServerLinkInfo) Links.fromString(domainString);

        if (link instanceof HubInfo) {
            HubInfo hubLink = (HubInfo) link;
            Endpoint endpoint = hubLink.getEndpoint();

            User user = new HeloUser(userConfig);
            Client client = new Client(endpoint, user, HUB);
            client.connect();

            IAsymmetricKeyStorage publicKeyFromLink = hubLink.getKeyStorage();

            if (publicKeyFromLink != null) {
                client.setServerKey(publicKeyFromLink);

            } else {
                IAsymmetricKeyStorage publicKetFromFile = userConfig.getPublicKey(endpoint);

                if (publicKetFromFile != null) {
                    client.setServerKey(publicKetFromFile);
                } else {
                    client.getPublicKeyFromServer();
                    IKeyStorage keyStorage = user.globalKeyStorage.get(client.getServerEncryption());
                    userConfig.addKey(endpoint, Objects.requireNonNull(keyStorage));
                }
            }
            client.sendSymmetricKey();

            getReceiveThread(client).start();

            while (true) {
                System.out.printf("[%s] ", endpoint);
                String input = scanner.nextLine();

                try {
                    HeadersBuilder headersBuilder = new HeadersBuilder().set(AES).set(MSG);

                    if (input.equalsIgnoreCase("exit")) {
                        ExitMessage exitRequest = new ExitMessage(headersBuilder);
                        client.sendMessage(exitRequest, client.getServerEncryption());
                        client.disconnect();
                        break;
                    } else if (!input.isEmpty()) {
                        IMessage request = handle(input, headersBuilder);
                        client.sendMessage(request, client.getServerEncryption());
                    }
                } catch (IOException | ReadingKeyException | EncryptionException | KeyStorageNotFoundException |
                         UnexpectedSocketDisconnect e) {
                    LOGGER.error("Error on console reading and sending cycle", e);
                    throw e;
                }
            }

            LOGGER.info("Exiting...");
            client.close();
            LOGGER.info("Closed");
        }

    }
}
