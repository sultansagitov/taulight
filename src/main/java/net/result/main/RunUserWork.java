package net.result.main;

import net.result.openhelo.HeloType;
import net.result.openhelo.exceptions.WrongTypeException;
import net.result.openhelo.messages.*;
import net.result.openhelo.HeloUser;
import net.result.sandnode.client.Client;
import net.result.sandnode.config.ClientConfigSingleton;
import net.result.sandnode.exceptions.CreatingKeyException;
import net.result.sandnode.exceptions.NoSuchReqHandler;
import net.result.sandnode.exceptions.ReadingKeyException;
import net.result.sandnode.exceptions.encryption.CannotUseEncryption;
import net.result.sandnode.exceptions.encryption.DecryptionException;
import net.result.sandnode.exceptions.encryption.EncryptionException;
import net.result.sandnode.exceptions.encryption.NoSuchEncryptionException;
import net.result.sandnode.messages.*;
import net.result.sandnode.User;
import net.result.sandnode.util.encryption.Encryption;
import net.result.sandnode.util.encryption.GlobalKeyStorage;
import net.result.sandnode.util.encryption.asymmetric.AsymmetricKeyStorage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Scanner;

import static java.nio.charset.StandardCharsets.US_ASCII;
import static net.result.sandnode.messages.util.NodeType.HUB;
import static net.result.sandnode.util.encryption.Encryption.AES;

public class RunUserWork implements IWork {
    private static final Logger LOGGER = LogManager.getLogger(RunUserWork.class);

    @Override
    public void run() throws ReadingKeyException, EncryptionException, IOException, NoSuchEncryptionException,
            DecryptionException, NoSuchReqHandler, CreatingKeyException, CannotUseEncryption, NoSuchAlgorithmException {
        Scanner scanner = new Scanner(System.in);
        GlobalKeyStorage globalKeyStorage = new GlobalKeyStorage();

        System.out.print("host: ");
        String host = scanner.nextLine();
        System.out.print("port: ");
        int port = Integer.parseInt(scanner.nextLine());

        User user = new HeloUser(globalKeyStorage);
        Client client = new Client(host, port, user, HUB);
        client.connect();
        AsymmetricKeyStorage publicKey = ClientConfigSingleton.getPublicKey(host, port);
        Encryption publicKeyEncryption = publicKey != null ? publicKey.encryption() : null;

        if (publicKeyEncryption == null) {
            publicKeyEncryption = client.getKeys();
        } else {
            globalKeyStorage.set(publicKeyEncryption, publicKey);
        }

        Thread receiveThread = new Thread(() -> {
            try {
                while (client.isConnected()) {
                    IMessage response = client.receiveMessage();
                    if (response == null) break;

                    ByteArrayInputStream in = new ByteArrayInputStream(response.getBody());
                    HeloType heloType;

                    try {
                        heloType = HeloType.fromByte((byte) in.read());
                    } catch (WrongTypeException e) {
                        throw new RuntimeException(e);
                    }

                    HeloMessage heloMessage = heloType.fromBytes(in.readAllBytes());

                    switch (heloType) {
                        case ECHO, FORWARD -> {
                            LOGGER.info("From server: {}", ((TextMessage) heloMessage).data);
                        }
                        case ONLINE_RESPONSE -> {
                            LOGGER.info("Online users: {}", ((OnlineResponseMessage) heloMessage).users);
                        }
                    }
                }
            } catch (ReadingKeyException | DecryptionException | NoSuchEncryptionException | NoSuchAlgorithmException |
                     NoSuchReqHandler | EncryptionException e) {
                LOGGER.error("Error on receiving message thread", e);
            }
        });
        receiveThread.start();

        while (true) {
            System.out.printf("[%s:%d] ", host, port);
            String input = scanner.nextLine();


            try {
                HeadersBuilder headersBuilder = new HeadersBuilder().set(AES);

                if (input.equalsIgnoreCase("exit")) {
                    ExitMessage exitRequest = new ExitMessage(headersBuilder);
                    client.sendMessage(exitRequest, publicKeyEncryption);
                    client.disconnect();
                    break;
                } else if (!input.isEmpty()) {
                    HeloMessage helorequest;

                    if (input.equalsIgnoreCase("getonline")) {
                        helorequest = new OnlineMessage();

                    } else if (input.startsWith("forward ")) {
                        String data = input.substring(input.indexOf(" ") + 1);
                        helorequest = new ForwardMessage(data);

                    } else {
                        helorequest = new EchoMessage(input);
                    }

                    IMessage request = new SandnodeMessageAdapter(headersBuilder, helorequest);
                    if (publicKeyEncryption != null)
                        client.sendMessage(request, publicKeyEncryption);
                    else
                        LOGGER.info("Message was not sent");
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
