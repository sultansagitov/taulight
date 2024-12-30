package net.result.sandnode.chain.client;

import net.result.sandnode.exceptions.*;
import net.result.sandnode.messages.IMessage;
import net.result.sandnode.messages.types.ErrorMessage;
import net.result.sandnode.messages.types.PublicKeyRequest;
import net.result.sandnode.messages.types.PublicKeyResponse;
import net.result.sandnode.util.IOControl;
import net.result.sandnode.chain.Chain;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static net.result.sandnode.messages.util.MessageType.ERR;

public class PublicKeyClientChain extends Chain {
    private static final Logger LOGGER = LogManager.getLogger(PublicKeyClientChain.class);

    public PublicKeyClientChain(IOControl io) {
        super(io);
    }

    @Override
    public void start() throws InterruptedException, EncryptionTypeException, NoSuchEncryptionException,
            CreatingKeyException, ExpectedMessageException {
        IMessage request = new PublicKeyRequest();
        send(request);

        IMessage response = queue.take();

        if (response.getHeaders().getType() == ERR) {
            ErrorMessage errorMessage = new ErrorMessage(response);
            request.getHeaders().setChainID(getID());
            LOGGER.info("Handle error {} : {}", errorMessage.error.code, errorMessage.error.desc);
            return;
        }

        PublicKeyResponse publicKeyResponse = new PublicKeyResponse(response);
        io.setServerKey(publicKeyResponse.keyStorage);
    }
}
