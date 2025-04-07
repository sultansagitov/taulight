package net.result.taulight.chain.receiver;

import net.result.sandnode.chain.ReceiverChain;
import net.result.sandnode.chain.receiver.ServerChain;
import net.result.sandnode.db.Member;
import net.result.sandnode.exception.DatabaseException;
import net.result.sandnode.exception.ImpossibleRuntimeException;
import net.result.sandnode.exception.error.AddressedMemberNotFoundException;
import net.result.sandnode.exception.error.UnauthorizedException;
import net.result.sandnode.message.util.Headers;
import net.result.sandnode.message.util.MessageTypes;
import net.result.sandnode.serverclient.Session;
import net.result.taulight.SysMessages;
import net.result.taulight.TauAgentProtocol;
import net.result.taulight.TauHubProtocol;
import net.result.taulight.dto.ChatMessageViewDTO;
import net.result.taulight.db.*;
import net.result.sandnode.exception.error.NotFoundException;
import net.result.sandnode.exception.error.NoEffectException;
import net.result.taulight.group.TauGroupManager;
import net.result.taulight.message.types.ReactionRequest;
import net.result.sandnode.message.types.HappyMessage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;
import java.util.Optional;

public class ReactionServerChain extends ServerChain implements ReceiverChain {
    private static final Logger LOGGER = LogManager.getLogger(ReactionServerChain.class);

    public ReactionServerChain(Session session) {
        super(session);
    }

    @Override
    public void sync() throws Exception {
        TauDatabase database = (TauDatabase) session.server.serverConfig.database();
        TauGroupManager manager = (TauGroupManager) session.server.serverConfig.groupManager();

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
            ReactionEntry newReaction = new ReactionEntry(
                database, UUID.randomUUID(), ZonedDateTime.now(), message.id(), reactionType.id(), session.member.nickname()
            );
            database.saveReactionEntry(newReaction);
            LOGGER.info("Reaction added: {} to message {}", reactionType.name(), request.getMessageID());
        } else {
            ReactionEntry reactionToRemove = currentReactions.stream()
                .filter(entry -> entry.nickname().equals(session.member.nickname()) && entry.reactionTypeId().equals(reactionType.id()))
                .findFirst()
                .orElseThrow(NoEffectException::new);

            database.removeReactionEntry(reactionToRemove);
            LOGGER.info("Reaction removed: {} from message {}", reactionType.name(), request.getMessageID());
        }
        sendFin(new HappyMessage());
    }
}