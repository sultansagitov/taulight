package net.result.sandnode.server.handlers;

import net.result.sandnode.exceptions.ReadingKeyException;
import net.result.sandnode.messages.HeadersBuilder;
import net.result.sandnode.messages.RawMessage;
import net.result.sandnode.messages.util.Connection;
import net.result.sandnode.server.ServerError;
import net.result.sandnode.server.Session;
import net.result.sandnode.server.commands.ICommand;
import net.result.sandnode.server.commands.ResponseCommand;
import net.result.sandnode.util.encryption.GlobalKeyStorage;
import net.result.sandnode.util.encryption.asymmetric.AsymmetricKeyStorage;
import net.result.sandnode.util.encryption.asymmetric.interfaces.IAsymmetricConvertor;
import net.result.sandnode.util.encryption.asymmetric.rsa.RSAPublicKeyConvertor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static java.nio.charset.StandardCharsets.US_ASCII;
import static net.result.sandnode.messages.util.MessageType.PUBLICKEY;
import static net.result.sandnode.util.encryption.Encryption.NO;
import static net.result.sandnode.util.encryption.Encryption.RSA;

public class RsaPublicKeyHandler implements IProtocolHandler {
    private static final Logger LOGGER = LogManager.getLogger(RsaPublicKeyHandler.class);
    private static final RsaPublicKeyHandler instance = new RsaPublicKeyHandler();

    private RsaPublicKeyHandler() {
    }

    public static RsaPublicKeyHandler getInstance() {
        return instance;
    }

    @Override
    public @Nullable ICommand getCommand(@NotNull RawMessage request, @NotNull List<Session> sessionList, @NotNull Session session, @NotNull GlobalKeyStorage globalKeyStorage) {
        Connection opposite = request.getConnection().getOpposite();
        HeadersBuilder headersBuilder = new HeadersBuilder()
                .set(opposite)
                .set(PUBLICKEY)
                .set(NO)
                .set("application/x-pem-file")
                .set("encryption", "" + RSA.asByte());

        RawMessage response = new RawMessage(headersBuilder);
        String pem;

        try {
            AsymmetricKeyStorage keyStorage = globalKeyStorage.getRSAKeyStorage();
            IAsymmetricConvertor convertor = RSAPublicKeyConvertor.getInstance();
            pem = convertor.toPEM(keyStorage);
        } catch (ReadingKeyException e) {
            LOGGER.error("Unknown", e);
            return ServerError.UNKNOWN_ENCRYPTION.sendError(opposite);
        }

        response.setBody(pem.getBytes(US_ASCII));
        return new ResponseCommand(response);
    }
}