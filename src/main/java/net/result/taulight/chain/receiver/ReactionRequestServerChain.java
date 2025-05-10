package net.result.taulight.chain.receiver;

import net.result.sandnode.chain.ReceiverChain;
import net.result.sandnode.chain.ServerChain;
import net.result.sandnode.exception.error.NoEffectException;
import net.result.sandnode.exception.error.UnauthorizedException;
import net.result.sandnode.exception.error.UnhandledMessageTypeException;
import net.result.sandnode.group.Group;
import net.result.sandnode.serverclient.Session;
import net.result.taulight.chain.sender.ReactionResponseServerChain;
import net.result.sandnode.exception.error.NotFoundException;
import net.result.taulight.db.*;
import net.result.taulight.message.types.ReactionRequest;
import net.result.sandnode.message.types.HappyMessage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ReactionRequestServerChain extends ServerChain implements ReceiverChain {
    private static final Logger LOGGER = LogManager.getLogger(ReactionRequestServerChain.class);

    public ReactionRequestServerChain(Session session) {
        super(session);
    }

    @Override
    public void sync() throws Exception {
        TauDatabase database = (TauDatabase) session.server.serverConfig.database();
        MessageRepository messageRepo = session.server.container.get(MessageRepository.class);

        ReactionRequest request = new ReactionRequest(queue.take());

        if (session.member == null) {
            throw new UnauthorizedException();
        }

        String nickname = session.member.nickname();

        MessageEntity message = messageRepo
                .findById(request.getMessageID())
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


        Group notReactionReceiver = session.server.serverConfig.groupManager().getGroup("#not_reaction_receiver");

        if (request.isReact()) {
            ReactionEntryEntity re = database.createReactionEntry(session.member.tauMember(), message, reactionType);
            LOGGER.info("Reaction added: {} to message {} by {}", reactionType.name(), message.id(), nickname);
            for (Session s : session.server.node.getAgents()) {
                if (!notReactionReceiver.contains(s)) {
                    var chain = new ReactionResponseServerChain(s);
                    s.io.chainManager.linkChain(chain);
                    try {
                        chain.reaction(re, s == session);
                    } catch (UnhandledMessageTypeException e) {
                        s.addToGroup(notReactionReceiver);
                    }
                    s.io.chainManager.removeChain(chain);
                }
            }
        } else {
            if (database.removeReactionEntry(message, session.member.tauMember(), reactionType)) {
                for (Session s : session.server.node.getAgents()) {
                    if (!notReactionReceiver.contains(s)) {
                        var chain = new ReactionResponseServerChain(s);
                        s.io.chainManager.linkChain(chain);
                        try {
                            chain.unreaction(nickname, message, reactionType, s == session);
                        } catch (UnhandledMessageTypeException e) {
                            s.addToGroup(notReactionReceiver);
                        }
                        s.io.chainManager.removeChain(chain);
                    }
                }

                LOGGER.info("Reaction removed: {} from message {}", reactionType.name(), message.id());
            } else {
                throw new NoEffectException();
            }
        }

        sendFin(new HappyMessage());
    }
}