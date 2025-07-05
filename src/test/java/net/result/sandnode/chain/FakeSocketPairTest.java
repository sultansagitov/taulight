package net.result.sandnode.chain;

import org.junit.jupiter.api.Test;

import java.io.*;
import java.net.Socket;

import static org.junit.jupiter.api.Assertions.*;

public class FakeSocketPairTest {

    @Test
    void testMessageExchange() throws IOException {
        FakeSocketPair pair = new FakeSocketPair();
        Socket client = pair.socket1;
        Socket server = pair.socket2;

        BufferedWriter clientOut = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()));
        BufferedReader clientIn = new BufferedReader(new InputStreamReader(client.getInputStream()));

        BufferedWriter serverOut = new BufferedWriter(new OutputStreamWriter(server.getOutputStream()));
        BufferedReader serverIn = new BufferedReader(new InputStreamReader(server.getInputStream()));

        clientOut.write("Hello Server!\n");
        clientOut.flush();
        String receivedByServer = serverIn.readLine();
        assertEquals("Hello Server!", receivedByServer);

        serverOut.write("Hello Client!\n");
        serverOut.flush();
        String receivedByClient = clientIn.readLine();
        assertEquals("Hello Client!", receivedByClient);

        client.close();
        server.close();
    }
}
