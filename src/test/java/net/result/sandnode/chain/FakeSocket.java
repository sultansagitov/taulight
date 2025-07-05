package net.result.sandnode.chain;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;

public class FakeSocket extends Socket {
    private final InputStream inputStream;
    private final OutputStream outputStream;

    public FakeSocket(InputStream in, OutputStream out) {
        this.inputStream = in;
        this.outputStream = out;
    }

    @Override
    public InetAddress getInetAddress() {
        return InetAddress.getLoopbackAddress();
    }

    @Override
    public int getPort() {
        return 7357;
    }

    @Override
    public InputStream getInputStream() {
        return inputStream;
    }

    @Override
    public OutputStream getOutputStream() {
        return outputStream;
    }

    @Override
    public void close() throws IOException {
        inputStream.close();
        outputStream.close();
    }
}
