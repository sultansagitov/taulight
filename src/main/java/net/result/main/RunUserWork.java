package net.result.main;

import net.result.openhelo.HeloType;
import net.result.openhelo.HeloUser;
import net.result.openhelo.exceptions.WrongTypeException;
import net.result.openhelo.messages.*;
import net.result.sandnode.User;
import net.result.sandnode.client.Client;
import net.result.sandnode.config.UserConfig;
import net.result.sandnode.exceptions.CreatingKeyException;
import net.result.sandnode.exceptions.NoSuchReqHandler;
import net.result.sandnode.exceptions.ReadingKeyException;
import net.result.sandnode.exceptions.encryption.CannotUseEncryption;
import net.result.sandnode.exceptions.encryption.DecryptionException;
import net.result.sandnode.exceptions.encryption.EncryptionException;
import net.result.sandnode.exceptions.encryption.NoSuchEncryptionException;
import net.result.sandnode.messages.ExitMessage;
import net.result.sandnode.messages.HeadersBuilder;
import net.result.sandnode.messages.IMessage;
import net.result.sandnode.util.Endpoint;
import net.result.sandnode.util.encryption.asymmetric.AsymmetricKeyStorage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;
import java.util.Scanner;

import static net.result.sandnode.messages.util.NodeType.HUB;
import static net.result.sandnode.util.encryption.Encryption.AES;

public class RunUserWork implements IWork {
    private static final Logger LOGGER = LogManager.getLogger(RunUserWork.class);

    private static void onMessage(@NotNull IMessage response) {
        byte[] responseBody = response.getBody();
        HeloType heloType;

        try {
            heloType = HeloType.fromByte(responseBody[0]);
        } catch (WrongTypeException e) {
            throw new RuntimeException(e);
        }

        byte[] bytes = Arrays.copyOfRange(responseBody, 1, responseBody.length);
        HeloMessage heloMessage = heloType.fromBytes(bytes);

        switch (heloType) {
            case ECHO, FORWARD -> LOGGER.info("From server: {}", ((TextMessage) heloMessage).data);
            case ONLINE_RESPONSE -> LOGGER.info("Online users: {}", ((OnlineResponseMessage) heloMessage).users);
        }

    }

    private static @NotNull Thread getReceiveThread(Client client) {
        Thread receiveThread = new Thread(() -> {
            try {
                while (client.isConnected()) {
                    IMessage response = client.receiveMessage();
                    if (response == null) break;
                    onMessage(response);
                }
            } catch (ReadingKeyException | DecryptionException | NoSuchEncryptionException | NoSuchReqHandler e) {
                LOGGER.error("Error on receiving message thread", e);
            }
        });
        receiveThread.setName("Receiving-thread");
        return receiveThread;
    }

    private static @NotNull IMessage handle(String input, HeadersBuilder headersBuilder) {
        HeloMessage helorequest;

        if (input.equalsIgnoreCase("getonline")) {
            helorequest = new OnlineMessage();

        } else if (input.startsWith("forward ")) {
            String data = input.substring(input.indexOf(" ") + 1);
            helorequest = new ForwardMessage(data);

        } else {
            helorequest = new EchoMessage(input);
        }

        return new SandnodeMessageAdapter(headersBuilder, helorequest);
    }

    @Override
    public void run() throws IOException, ReadingKeyException, NoSuchEncryptionException, DecryptionException,
            EncryptionException, NoSuchReqHandler, CreatingKeyException, CannotUseEncryption {
        Scanner scanner = new Scanner(System.in);
        UserConfig userConfig = new UserConfig();

        System.out.print("endpoint: ");
        String domainString = scanner.nextLine();
        Endpoint endpoint = Endpoint.getFromString(domainString, 52525);

        User user = new HeloUser(userConfig);
        Client client = new Client(endpoint, user, HUB);
        client.connect();

        AsymmetricKeyStorage publicKey = userConfig.getPublicKey(endpoint);
        if (publicKey != null) {
            client.encryptionOfServer = publicKey.encryption();
            user.globalKeyStorage.set(publicKey);
        } else {
            client.getPublicKeyFromServer();
            userConfig.addKey(endpoint, Objects.requireNonNull(user.globalKeyStorage.get(client.encryptionOfServer)));
        }

        client.sendSymmetricKey();

        getReceiveThread(client).start();

        while (true) {
            System.out.printf("[%s] ", endpoint);
            String input = scanner.nextLine();


            try {
                HeadersBuilder headersBuilder = new HeadersBuilder().set(AES);

                if (input.equalsIgnoreCase("exit")) {
                    ExitMessage exitRequest = new ExitMessage(headersBuilder);
                    client.sendMessage(exitRequest, client.encryptionOfServer);
                    client.disconnect();
                    break;
                } else if (!input.isEmpty()) {
                    IMessage request = handle(input, headersBuilder);
                    client.sendMessage(request, client.encryptionOfServer);
                }
            } catch (IOException | ReadingKeyException | EncryptionException e) {
                LOGGER.error("Error on console reading and sending cycle", e);
                throw new RuntimeException(e);
            }
        }

        LOGGER.info("Exiting...");
        client.close();
        LOGGER.info("Closed");
    }
}
