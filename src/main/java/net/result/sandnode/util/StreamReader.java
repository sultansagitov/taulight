package net.result.sandnode.util;

import net.result.sandnode.exceptions.InputStreamException;
import net.result.sandnode.exceptions.OutputStreamException;
import net.result.sandnode.exceptions.UnexpectedSocketDisconnectException;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;

public class StreamReader {
    public static byte readByte(@NotNull InputStream in, @NotNull String message) throws UnexpectedSocketDisconnectException {
        int integer;
        try {
            integer = in.read();
        } catch (IOException e) {
            throw new UnexpectedSocketDisconnectException(message);
        }
        if (integer == -1)
            throw new UnexpectedSocketDisconnectException(message);
        return (byte) integer;
    }

    public static short readShort(@NotNull InputStream in, @NotNull String message) throws UnexpectedSocketDisconnectException {
        byte[] bytes;
        try {
            bytes = in.readNBytes(2);
        } catch (IOException e) {
            throw new UnexpectedSocketDisconnectException(message);
        }
        if (bytes.length < 2)
            throw new UnexpectedSocketDisconnectException(message);
        return ByteBuffer.wrap(bytes).getShort();
    }

    public static int readInt(@NotNull InputStream in, @NotNull String message) throws UnexpectedSocketDisconnectException {
        byte[] bytes;
        try {
            bytes = in.readNBytes(4);
        } catch (IOException e) {
            throw new UnexpectedSocketDisconnectException(message);
        }
        if (bytes.length < 4)
            throw new UnexpectedSocketDisconnectException(message);
        return ByteBuffer.wrap(bytes).getInt();
    }

    public static byte @NotNull [] readN(@NotNull InputStream in, int length, @NotNull String message)
            throws UnexpectedSocketDisconnectException {
        byte[] bytes;
        try {
            bytes = in.readNBytes(length);
        } catch (IOException e) {
            throw new UnexpectedSocketDisconnectException(message);
        }
        if (bytes.length < length)
            throw new UnexpectedSocketDisconnectException(message);
        return bytes;
    }

    public static InputStream inputStream(Socket socket) throws InputStreamException {
        try {
            return socket.getInputStream();
        } catch (IOException e) {
            throw new InputStreamException(e);
        }
    }

    public static OutputStream outputStream(Socket socket) throws OutputStreamException {
        try {
            return socket.getOutputStream();
        } catch (IOException e) {
            throw new OutputStreamException(e);
        }
    }
}
