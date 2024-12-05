package net.result.sandnode;

import net.result.sandnode.exceptions.*;
import net.result.sandnode.messages.IMessage;
import net.result.sandnode.messages.types.*;
import net.result.sandnode.messages.util.Headers;
import net.result.sandnode.server.Session;
import net.result.sandnode.encryption.interfaces.IAsymmetricEncryption;
import net.result.sandnode.encryption.interfaces.IAsymmetricKeyStorage;
import net.result.sandnode.encryption.interfaces.ISymmetricEncryption;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

public class ServerProtocol {
    private static final Logger LOGGER = LogManager.getLogger(ServerProtocol.class);

    public static void sendPUB(@NotNull Session session) throws EncryptionException, KeyStorageNotFoundException,
            WrongEncryptionException, MessageSerializationException, MessageWriteException,
            UnexpectedSocketDisconnectException {
        IAsymmetricEncryption mainEncryption = session.server.node.nodeConfig.mainEncryption();
        IAsymmetricKeyStorage keyStorage = session.server.node.globalKeyStorage.getAsymmetric(mainEncryption);
        Headers headers = new Headers();
        PublicKeyResponse response = new PublicKeyResponse(headers, keyStorage);
        session.io.sendMessage(response);
    }

    public static void handleSYM(@NotNull Session session, @NotNull IMessage request) throws NoSuchEncryptionException,
            ExpectedMessageException, CannotUseEncryption {
        SymMessage message = new SymMessage(request);
        session.io.setSymmetricKey(message.symmetricKeyStorage);
        LOGGER.info("Symmetric key initialized");
    }

    public static void handleREG(@NotNull Session session, @NotNull IMessage request) throws ExpectedMessageException,
            EncryptionException, KeyStorageNotFoundException, MessageSerializationException, MessageWriteException,
            UnexpectedSocketDisconnectException {
        RegistrationRequest regMsg = new RegistrationRequest(request);
        session.member = session.server.database.registerMember(regMsg.getMemberID(), regMsg.getPassword());
        String token = session.server.tokenizer.tokenizeMember(session.member);

        ISymmetricEncryption symmetricKeyEncryption = session.server.node.nodeConfig.symmetricKeyEncryption();
        Headers headers = new Headers().set(symmetricKeyEncryption);
        RegistrationResponse response = new RegistrationResponse(headers, token);
        session.io.sendMessage(response);
    }

    public static void handleLOGIN(@NotNull Session session, @NotNull IMessage request) throws EncryptionException,
            KeyStorageNotFoundException, MessageSerializationException, MessageWriteException,
            UnexpectedSocketDisconnectException {
        TokenMessage msg = new TokenMessage(request);
        String token = msg.getToken();
        session.member = session.server.tokenizer.findMember(session.server.database, token).get();

        ISymmetricEncryption symmetricKeyEncryption = session.server.node.nodeConfig.symmetricKeyEncryption();
        Headers headers = new Headers().set(symmetricKeyEncryption);
        IMessage response = new LoginResponse(headers, session.member);
        session.io.sendMessage(response);
    }

    public static void sendAuthRequest(@NotNull Session session) throws EncryptionException,
            KeyStorageNotFoundException, MessageSerializationException, MessageWriteException,
            UnexpectedSocketDisconnectException {
        ISymmetricEncryption symmetricKeyEncryption = session.server.node.nodeConfig.symmetricKeyEncryption();
        Headers headers = new Headers().set(symmetricKeyEncryption);
        RequestMessage response = new RequestMessage(headers);
        session.io.sendMessage(response);
    }
}
