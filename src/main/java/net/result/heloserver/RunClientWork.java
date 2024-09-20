package net.result.heloserver;

import net.result.sandnode.client.Client;
import net.result.sandnode.exceptions.ReadingKeyException;
import net.result.sandnode.exceptions.encryption.DecryptionException;
import net.result.sandnode.exceptions.encryption.EncryptionException;
import net.result.sandnode.exceptions.encryption.NoSuchEncryptionException;
import net.result.sandnode.messages.EXITMessage;
import net.result.sandnode.messages.HeadersBuilder;
import net.result.sandnode.messages.IMessage;
import net.result.sandnode.messages.JSONMessage;
import net.result.sandnode.util.encryption.GlobalKeyStorage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Scanner;

import static net.result.sandnode.messages.util.Connection.CLIENT2SERVER;
import static net.result.sandnode.messages.util.MessageType.MESSAGE;
import static net.result.sandnode.util.encryption.Encryption.NO;

public class RunClientWork implements IWork {
    private static final Logger LOGGER = LogManager.getLogger(RunClientWork.class);

    @Override
    public void run() throws NoSuchEncryptionException, ReadingKeyException, NoSuchAlgorithmException, DecryptionException, EncryptionException {
        Scanner scanner = new Scanner(System.in);
        GlobalKeyStorage globalKeyStorage = new GlobalKeyStorage();

        System.out.print("host: ");
        String host = scanner.nextLine();
        System.out.print("port: ");
        int port = Integer.parseInt(scanner.nextLine());

        Client client = new Client(host, port, globalKeyStorage);
        client.connect(NO);

        boolean sendNextMessage = true;
        while (sendNextMessage) {
            System.out.printf("[%s:%d] ", host, port);
            String input = scanner.nextLine();

            try {
                if (input.equalsIgnoreCase("exit")) {
                    sendNextMessage = false;
                    HeadersBuilder headersBuilder = new HeadersBuilder().set(CLIENT2SERVER);
                    client.sendMessage(new EXITMessage(headersBuilder));
                } else {
                    HeadersBuilder headersBuilder = new HeadersBuilder()
                            .set(CLIENT2SERVER)
                            .set(MESSAGE)
                            .set(NO);
                    JSONObject content = new JSONObject().put("data", input);
                    client.sendMessage(new JSONMessage(headersBuilder, content));

                    IMessage response = client.receiveMessage();
                    LOGGER.info("Server headers: {}", response.getHeaders());
                    LOGGER.info("Server body: {}", response.getBody());

                    try {
                        JSONObject object = new JSONObject(new String(response.getBody()));
                        LOGGER.info("JSON body: {}", object);
                    } catch (Exception ignored) {
                    }
                }
            } catch (IOException | ReadingKeyException | EncryptionException |
                     NoSuchAlgorithmException | NoSuchEncryptionException e) {
                throw new RuntimeException(e);
            }
        }

        LOGGER.info("Exiting...");
        client.close();
        LOGGER.info("Closed");
    }
}
