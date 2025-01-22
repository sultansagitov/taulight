package net.result.sandnode.chain.client;

import net.result.sandnode.exception.*;
import net.result.sandnode.message.IMessage;
import net.result.sandnode.message.types.ErrorMessage;
import net.result.sandnode.message.types.PublicKeyRequest;
import net.result.sandnode.message.types.PublicKeyResponse;
import net.result.sandnode.util.IOControl;
import net.result.sandnode.chain.Chain;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static net.result.sandnode.message.util.MessageTypes.ERR;

public class PublicKeyClientChain extends Chain {
    private static final Logger LOGGER = LogManager.getLogger(PublicKeyClientChain.class);

    public PublicKeyClientChain(IOControl io) {
        super(io);
    }

    @Override
    public void sync() throws InterruptedException, EncryptionTypeException, NoSuchEncryptionException,
            CreatingKeyException, ExpectedMessageException, DeserializationException {
        IMessage request = new PublicKeyRequest();
        send(request);

        IMessage response = queue.take();

        if (response.getHeaders().getType() == ERR) {
            ErrorMessage errorMessage = new ErrorMessage(response);
            request.getHeaders().setChainID(getID());
            LOGGER.info("Handle error {} : {}", errorMessage.error.getCode(), errorMessage.error.getDescription());
            return;
        }

        PublicKeyResponse publicKeyResponse = new PublicKeyResponse(response);
        io.setServerKey(publicKeyResponse.keyStorage);
    }
}
