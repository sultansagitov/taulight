package net.result.taulight.chain.receiver;

import net.result.sandnode.chain.ReceiverChain;
import net.result.sandnode.chain.receiver.ServerChain;
import net.result.sandnode.exception.error.UnauthorizedException;
import net.result.sandnode.serverclient.Session;
import net.result.taulight.dto.ChatMessageViewDTO;
import net.result.taulight.db.*;
import net.result.sandnode.exception.error.NotFoundException;
import net.result.sandnode.exception.error.NoEffectException;
import net.result.taulight.message.types.ReactionRequest;
import net.result.sandnode.message.types.HappyMessage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

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

        ChatMessageViewDTO message = database
                .findMessage(request.getMessageID())
                .orElseThrow(NotFoundException::new);

        String[] packageParts = request.getReactionType().split(":");
        if (packageParts.length != 2) {
            throw new IllegalArgumentException("Invalid reaction type format. Expected format 'package:reaction'.");
        }
        String packageName = packageParts[0];
        String reactionTypeName = packageParts[1];

        ReactionType reactionType = database.getReactionTypesByPackage(packageName)
                .stream()
                .filter(type -> type.name().equals(reactionTypeName))
                .findFirst()
                .orElseThrow(NotFoundException::new);

        List<ReactionEntry> currentReactions = database.getReactionsByMessage(message);

        if (request.isReact()) {
            var newReaction = new ReactionEntry(database, message.id(), reactionType.id(), session.member.nickname());
            database.saveReactionEntry(newReaction);
            LOGGER.info("Reaction added: {} to message {}", reactionType.name(), request.getMessageID());
        } else {
            ReactionEntry reactionToRemove = currentReactions.stream()
                    .filter(entry -> entry.nickname().equals(session.member.nickname()))
                    .filter(entry -> entry.reactionTypeId().equals(reactionType.id()))
                    .findFirst()
                    .orElseThrow(NoEffectException::new);

            database.removeReactionEntry(reactionToRemove);
            LOGGER.info("Reaction removed: {} from message {}", reactionType.name(), request.getMessageID());
        }
        sendFin(new HappyMessage());
    }
}