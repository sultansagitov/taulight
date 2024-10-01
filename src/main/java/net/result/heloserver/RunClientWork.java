package net.result.heloserver;

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
import net.result.sandnode.util.encryption.Encryption;
import net.result.sandnode.util.encryption.GlobalKeyStorage;
import net.result.sandnode.util.encryption.asymmetric.AsymmetricEncryptionFactory;
import net.result.sandnode.util.encryption.asymmetric.AsymmetricKeyStorage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Scanner;

import static java.nio.charset.StandardCharsets.US_ASCII;
import static net.result.sandnode.messages.util.Connection.CLIENT2SERVER;
import static net.result.sandnode.messages.util.MessageType.*;
import static net.result.sandnode.util.encryption.Encryption.*;

public class RunClientWork implements IWork {
    private static final Logger LOGGER = LogManager.getLogger(RunClientWork.class);

    @Override
    public void run() throws
            ReadingKeyException, EncryptionException, IOException, NoSuchEncryptionException, DecryptionException,
            NoSuchReqHandler, CreatingKeyException, CannotUseEncryption, NoSuchAlgorithmException {
        ClientConfigSingleton.getInstance();
        Scanner scanner = new Scanner(System.in);
        GlobalKeyStorage globalKeyStorage = new GlobalKeyStorage();

        System.out.print("host: ");
        String host = scanner.nextLine();
        System.out.print("port: ");
        int port = Integer.parseInt(scanner.nextLine());

        Client client = new Client(host, port, globalKeyStorage);
        client.connect();
        AsymmetricKeyStorage publicKey = ClientConfigSingleton.getPublicKey(host, port);

        Encryption publicKeyEncryption =
                (publicKey == null)
                        ? client.getKeys()
                        : AsymmetricEncryptionFactory.setKeyStorage(globalKeyStorage, publicKey);
        Thread receiveThread = new Thread(() -> {
            try {
                while (client.isConnected()) {
                    IMessage response = client.receiveMessage();
                    if (response == null) {
                        break;
                    }
                    JSONObject object = new JSONObject(new String(response.getBody(), US_ASCII));
                    String hostString = "???";
                    try {
                        hostString = object.getString("host");
                    } catch (JSONException ignored) {
                    }
                    int portInt = -1;
                    try {
                        portInt = object.getInt("port");
                    } catch (JSONException ignored) {
                    }
                    System.out.printf("<%s:%d> %s%n", hostString, portInt, object);
                }
            } catch (ReadingKeyException | DecryptionException | NoSuchEncryptionException |
                     NoSuchAlgorithmException | NoSuchReqHandler | EncryptionException e) {
                LOGGER.error("Error receiving message", e);
            }
        });
        receiveThread.start();

        boolean sendNextMessage = true;
        while (sendNextMessage) {
            System.out.printf("[%s:%d] ", host, port);
            String input = scanner.nextLine();


            try {
                HeadersBuilder headersBuilder = new HeadersBuilder().set(CLIENT2SERVER).set(AES);

                if (input.equalsIgnoreCase("exit")) {
                    sendNextMessage = false;
                    client.sendMessage(new ExitMessage(headersBuilder), publicKeyEncryption);
                    client.disconnect();
                } else if (!input.isEmpty()) {
                    IMessage request;

                    if (input.equalsIgnoreCase("getonline")) {
                        headersBuilder.set(TMPONLINE);
                        RawMessage rawrequest = new RawMessage(headersBuilder);
                        rawrequest.setBody(new byte[0]);
                        request = rawrequest;
                    } else if (input.startsWith("forward")) {
                        headersBuilder.set(FORWARD);
                        JSONObject content = new JSONObject()
                                .put("data", input.substring(input.indexOf(" ") + 1));
                        request = new JSONMessage(headersBuilder, content);
                    } else {
                        headersBuilder.set(MESSAGE);
                        JSONObject content = new JSONObject().put("data", input);
                        request = new JSONMessage(headersBuilder, content);
                    }
                    LOGGER.debug("Sending {}", publicKeyEncryption);
                    if (publicKeyEncryption != null) {
                        client.sendMessage(request, publicKeyEncryption);
                    } else {
                        LOGGER.info("Message was not sent");
                    }
                }
            } catch (IOException | ReadingKeyException | EncryptionException e) {
                throw new RuntimeException(e);
            }
        }

        LOGGER.info("Exiting...");
        client.close();
        LOGGER.info("Closed");
    }
}
