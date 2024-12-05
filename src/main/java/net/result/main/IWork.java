package net.result.main;

import net.result.sandnode.exceptions.*;
import net.result.sandnode.exceptions.CannotUseEncryption;
import net.result.sandnode.exceptions.DecryptionException;
import net.result.sandnode.exceptions.EncryptionException;
import net.result.sandnode.exceptions.NoSuchEncryptionException;
import net.result.sandnode.exceptions.FSException;

import java.net.URISyntaxException;

public interface IWork {
    void run() throws NoSuchEncryptionException, ConfigurationException, CannotUseEncryption, CreatingKeyException,
            KeyStorageNotFoundException, InvalidLinkSyntaxException, URISyntaxException, UnexpectedSocketDisconnectException,
            EncryptionException, DecryptionException, NoSuchMessageTypeException, KeyNotCreatedException, ExpectedMessageException,
            FSException, MessageSerializationException, MessageWriteException, ConnectionException, OutputStreamException,
            InputStreamException, ServerStartException;
}
