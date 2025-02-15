package net.result.taulight.chain.client;

import net.result.sandnode.chain.client.ClientChain;
import net.result.sandnode.error.SandnodeError;
import net.result.sandnode.exception.*;
import net.result.sandnode.message.RawMessage;
import net.result.sandnode.message.types.ErrorMessage;
import net.result.sandnode.message.types.HappyMessage;
import net.result.sandnode.message.util.MessageTypes;
import net.result.sandnode.serverclient.ClientMember;
import net.result.sandnode.error.Errors;
import net.result.sandnode.util.IOController;
import net.result.taulight.error.TauErrors;
import net.result.taulight.exception.ChatNotFoundException;
import net.result.taulight.message.types.ChannelRequest;

import java.util.UUID;

public class ChannelClientChain extends ClientChain {
    public ChannelClientChain(IOController io) {
        super(io);
    }

    @Override
    public boolean isChainStartAllowed() {
        return false;
    }

    @Override
    public void sync() {
        throw new ImpossibleRuntimeException("This chain should not be started");
    }

    public void sendNewChannelRequest(String title) throws InterruptedException, ExpectedMessageException {
        send(ChannelRequest.newChannel(title));
        RawMessage raw = queue.take();
        new HappyMessage(raw);
    }

    public void sendLeaveRequest(UUID chatID) throws InterruptedException, ExpectedMessageException,
            ChatNotFoundException, DeserializationException, TooFewArgumentsException, WrongAddressException,
            UnauthorizedException {
        send(ChannelRequest.leave(chatID));
        RawMessage raw = queue.take();

        if (raw.getHeaders().getType() == MessageTypes.ERR) {
            ErrorMessage errorMessage = new ErrorMessage(raw);
            SandnodeError error = errorMessage.error;
            if (error instanceof Errors sys) {
                switch (sys) {
                    case TOO_FEW_ARGS -> throw new TooFewArgumentsException();
                    case WRONG_ADDRESS -> throw new WrongAddressException();
                    case UNAUTHORIZED -> throw new UnauthorizedException();
                    case SERVER_ERROR -> throw new UnknownSandnodeErrorException();
                }
            }

            if (error == TauErrors.CHAT_NOT_FOUND) {
                throw new ChatNotFoundException(chatID);
            }
        }

        new HappyMessage(raw);
    }

    public void sendAddMemberRequest(UUID chatID, ClientMember member)
            throws InterruptedException, DeserializationException, ChatNotFoundException, TooFewArgumentsException,
            AddressedMemberNotFoundException, WrongAddressException, UnauthorizedException, ExpectedMessageException {
        send(ChannelRequest.addMember(chatID, member));
        RawMessage raw = queue.take();
        if (raw.getHeaders().getType() == MessageTypes.ERR) {
            SandnodeError error = new ErrorMessage(raw).error;

            if (error == TauErrors.CHAT_NOT_FOUND) {
                throw new ChatNotFoundException(chatID);
            }

            if (error instanceof Errors snErr) {
                switch (snErr) {
                    case TOO_FEW_ARGS -> throw new TooFewArgumentsException();
                    case ADDRESSED_MEMBER_NOT_FOUND -> throw new AddressedMemberNotFoundException(member);
                    case WRONG_ADDRESS -> throw new WrongAddressException();
                    case UNAUTHORIZED -> throw new UnauthorizedException();
                }
            }
        }

        new HappyMessage(raw);
    }
}
