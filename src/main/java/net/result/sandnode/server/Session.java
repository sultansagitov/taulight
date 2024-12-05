package net.result.sandnode.server;

import net.result.sandnode.encryption.interfaces.IEncryption;
import net.result.sandnode.exceptions.*;
import net.result.sandnode.messages.util.Connection;
import net.result.sandnode.util.IOControl;
import net.result.sandnode.util.db.IMember;
import net.result.sandnode.encryption.GlobalKeyStorage;
import org.jetbrains.annotations.NotNull;

import java.net.Socket;

import static net.result.sandnode.encryption.Encryption.NONE;

public class Session {
    public final GlobalKeyStorage globalKeyStorage;
    public final SandnodeServer server;
    public final Socket socket;
    public final IOControl io;
    public IMember member;
    public IEncryption encryption = NONE;

    public Session(
            @NotNull SandnodeServer server,
            @NotNull Connection connection,
            @NotNull Socket socket,
            @NotNull GlobalKeyStorage globalKeyStorage
    ) throws InputStreamException, OutputStreamException {
        this.server = server;
        this.socket = socket;
        this.globalKeyStorage = globalKeyStorage.copy();
        io = new IOControl(socket, connection, this.globalKeyStorage);
    }

    @Override
    public String toString() {
        return String.format("<%s %s %s>", getClass().getSimpleName(), IOControl.getIP(socket), member);
    }
}
