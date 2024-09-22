package net.result.heloserver;

import net.result.sandnode.client.Client;
import net.result.sandnode.exceptions.NoSuchReqHandler;
import net.result.sandnode.exceptions.ReadingKeyException;
import net.result.sandnode.exceptions.encryption.DecryptionException;
import net.result.sandnode.exceptions.encryption.EncryptionException;
import net.result.sandnode.exceptions.encryption.NoSuchEncryptionException;
import net.result.sandnode.messages.*;
import net.result.sandnode.util.encryption.GlobalKeyStorage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Scanner;

import static java.nio.charset.StandardCharsets.US_ASCII;
import static net.result.sandnode.messages.util.Connection.CLIENT2SERVER;
import static net.result.sandnode.messages.util.MessageType.*;
import static net.result.sandnode.util.encryption.Encryption.NO;

public class RunClientWork implements IWork {
    private static final Logger LOGGER = LogManager.getLogger(RunClientWork.class);

    @Override
    public void run() {
        Scanner scanner = new Scanner(System.in);
        GlobalKeyStorage globalKeyStorage = new GlobalKeyStorage();

        System.out.print("host: ");
        String host = scanner.nextLine();
        System.out.print("port: ");
        int port = Integer.parseInt(scanner.nextLine());

        Client client = new Client(host, port, globalKeyStorage);
        client.connect(NO);

        Thread receiveThread = new Thread(() -> {
            try {
                while (true) {
                    IMessage response = client.receiveMessage();
                    JSONObject object = new JSONObject(new String(response.getBody(), US_ASCII));
                    System.out.printf("<%s:%d> %s%n", object.getString("host"), object.getInt("port"), object);
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
                HeadersBuilder headersBuilder = new HeadersBuilder().set(CLIENT2SERVER);

                if (input.equalsIgnoreCase("exit")) {
                    sendNextMessage = false;
                    client.sendMessage(new EXITMessage(headersBuilder));
                } else {
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
                        headersBuilder.set(MESSAGE).set(NO);
                        JSONObject content = new JSONObject().put("data", input);
                        request = new JSONMessage(headersBuilder, content);
                    }

                    client.sendMessage(request);
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
