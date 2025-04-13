package net.result.taulight.chain.receiver;

import net.result.sandnode.chain.ReceiverChain;
import net.result.sandnode.chain.receiver.ServerChain;
import net.result.sandnode.exception.error.NoEffectException;
import net.result.sandnode.exception.error.UnauthorizedException;
import net.result.sandnode.serverclient.Session;
import net.result.taulight.db.*;
import net.result.sandnode.exception.error.NotFoundException;
import net.result.taulight.message.types.ReactionRequest;
import net.result.sandnode.message.types.HappyMessage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ReactionServerChain extends ServerChain implements ReceiverChain {
    private static final Logger LOGGER = LogManager.getLogger(ReactionServerChain.class);

    public ReactionServerChain(Session session) {
        super(session);
    }

    @Override
    public void sync() throws Exception {
        TauDatabase database = (TauDatabase) session.server.serverConfig.database();

        ReactionRequest request = new ReactionRequest(queue.take());

        if (session.member == null) {
            throw new UnauthorizedException();
        }

        MessageEntity message = database
                .findMessage(request.getMessageID())
                .orElseThrow(NotFoundException::new);

        String[] packageParts = request.getReactionType().split(":");
        if (packageParts.length != 2) {
            throw new IllegalArgumentException("Invalid reaction type format. Expected format 'package:reaction'.");
        }
        String packageName = packageParts[0];
        String reactionTypeName = packageParts[1];

        ReactionTypeEntity reactionType = database
                .getReactionTypesByPackage(packageName).stream()
                .filter(type -> type.name().equals(reactionTypeName))
                .findFirst()
                .orElseThrow(NotFoundException::new);

        if (request.isReact()) {
            database.createReactionEntry(session.member, message, reactionType);
            LOGGER.info("Reaction added: {} to message {} by {}",
                    reactionType.name(), message.id(), session.member.nickname());
        } else {
            if (database.removeReactionEntry(message, session.member, reactionType)) {
                LOGGER.info("Reaction removed: {} from message {}", reactionType.name(), message.id());
            } else {
                throw new NoEffectException();
            }

        }
        sendFin(new HappyMessage());
    }
}