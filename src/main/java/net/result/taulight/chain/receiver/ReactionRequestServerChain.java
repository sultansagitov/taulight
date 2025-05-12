package net.result.taulight.chain.receiver;

import net.result.sandnode.chain.ReceiverChain;
import net.result.sandnode.chain.ServerChain;
import net.result.sandnode.exception.error.NoEffectException;
import net.result.sandnode.exception.error.UnauthorizedException;
import net.result.sandnode.exception.error.UnhandledMessageTypeException;
import net.result.sandnode.group.Group;
import net.result.sandnode.group.GroupManager;
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
        GroupManager groupManager = session.server.container.get(GroupManager.class);
        MessageRepository messageRepo = session.server.container.get(MessageRepository.class);
        ReactionTypeRepository reactionTypeRepo = session.server.container.get(ReactionTypeRepository.class);
        ReactionEntryRepository reactionEntryRepo = session.server.container.get(ReactionEntryRepository.class);

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

        ReactionTypeEntity reactionType = reactionTypeRepo
                .findByPackageName(packageName).stream()
                .filter(type -> type.name().equals(reactionTypeName))
                .findFirst()
                .orElseThrow(NotFoundException::new);


        Group notReactionReceiver = groupManager.getGroup("#not_reaction_receiver");

        if (request.isReact()) {
            ReactionEntryEntity re = reactionEntryRepo.create(session.member.tauMember(), message, reactionType);
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
            if (reactionEntryRepo.delete(message, session.member.tauMember(), reactionType)) {
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