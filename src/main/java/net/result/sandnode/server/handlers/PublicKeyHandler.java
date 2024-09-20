package net.result.sandnode.server.handlers;

import net.result.sandnode.exceptions.ReadingKeyException;
import net.result.sandnode.messages.HeadersBuilder;
import net.result.sandnode.messages.IMessage;
import net.result.sandnode.messages.RawMessage;
import net.result.sandnode.server.ServerError;
import net.result.sandnode.util.encryption.Encryption;
import net.result.sandnode.util.encryption.GlobalKeyStorage;
import net.result.sandnode.util.encryption.asymmetric.AsymmetricEncryptionFactory;
import net.result.sandnode.util.encryption.asymmetric.interfaces.IAsymmetricConvertor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.security.NoSuchAlgorithmException;

import static java.nio.charset.StandardCharsets.US_ASCII;
import static net.result.sandnode.messages.util.MessageType.PUBLICKEY;

public class PublicKeyHandler implements IProtocolHandler {
    private static final Logger LOGGER = LogManager.getLogger(PublicKeyHandler.class);
    private final GlobalKeyStorage globalKeyStorage;

    public PublicKeyHandler(@NotNull GlobalKeyStorage globalKeyStorage) {
        this.globalKeyStorage = globalKeyStorage;
    }

    @Override
    public IMessage getResponse(@NotNull IMessage request) {
        HeadersBuilder headersBuilder = new HeadersBuilder()
                .set(request.getHeaders().getConnection().getOpposite())
                .set(PUBLICKEY)
                .set(Encryption.NO)
                .set("application/x-pem-file");

        final RawMessage response = new RawMessage(headersBuilder);
        final IAsymmetricConvertor convertor;
        final String pem;

        try {
            convertor = AsymmetricEncryptionFactory.getPublicConvertor(request.getHeaders().encryption);
            pem = convertor.toPEM(AsymmetricEncryptionFactory.getKeyStorage(globalKeyStorage, request.getHeaders().encryption));
        } catch (NoSuchAlgorithmException | ReadingKeyException e) {
            LOGGER.error("Unknown", e);
            return ServerError.UNKNOWN_ENCRYPTION.sendError();
        }

        response.setBody(pem.getBytes(US_ASCII));

        return response;
    }
}