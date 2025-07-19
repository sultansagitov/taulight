package net.result.sandnode.chain;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

public class FakeSocketPair {
    public final FakeSocket socket1;
    public final FakeSocket socket2;

    public FakeSocketPair() throws IOException {
        PipedOutputStream out1 = new PipedOutputStream();
        PipedInputStream in2 = new PipedInputStream(out1);

        PipedOutputStream out2 = new PipedOutputStream();
        PipedInputStream in1 = new PipedInputStream(out2);

        socket1 = new FakeSocket(in1, out1);
        socket2 = new FakeSocket(in2, out2);
    }
}
