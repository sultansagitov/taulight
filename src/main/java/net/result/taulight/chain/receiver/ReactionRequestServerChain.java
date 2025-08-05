package net.result.taulight.chain.receiver;

import net.result.sandnode.chain.ReceiverChain;
import net.result.sandnode.chain.ServerChain;
import net.result.sandnode.cluster.Cluster;
import net.result.sandnode.cluster.ClusterManager;
import net.result.sandnode.exception.error.NoEffectException;
import net.result.sandnode.exception.error.NotFoundException;
import net.result.sandnode.exception.error.UnauthorizedException;
import net.result.sandnode.exception.error.UnhandledMessageTypeException;
import net.result.sandnode.message.RawMessage;
import net.result.sandnode.message.types.HappyMessage;
import net.result.sandnode.serverclient.Session;
import net.result.sandnode.util.JPAUtil;
import net.result.taulight.chain.sender.ReactionResponseServerChain;
import net.result.taulight.db.*;
import net.result.taulight.message.types.ReactionRequest;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ReactionRequestServerChain extends ServerChain implements ReceiverChain {
    private static final Logger LOGGER = LogManager.getLogger(ReactionRequestServerChain.class);

    public ReactionRequestServerChain(Session session) {
        super(session);
    }

    @Override
    public HappyMessage handle(RawMessage raw) throws Exception {
        ClusterManager clusterManager = session.server.container.get(ClusterManager.class);
        ReactionTypeRepository reactionTypeRepo = session.server.container.get(ReactionTypeRepository.class);
        ReactionEntryRepository reactionEntryRepo = session.server.container.get(ReactionEntryRepository.class);
        JPAUtil jpaUtil = session.server.container.get(JPAUtil.class);

        ReactionRequest request = new ReactionRequest(raw);

        if (session.member == null) {
            throw new UnauthorizedException();
        }

        String nickname = session.member.nickname();

        MessageEntity message = jpaUtil
                .find(MessageEntity.class, request.dto().messageID)
                .orElseThrow(NotFoundException::new);

        String[] packageParts = request.dto().reaction.split(":");
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


        Cluster notReactionReceiver = clusterManager.get("#not_reaction_receiver");

        if (request.dto().react) {
            ReactionEntryEntity re = reactionEntryRepo.create(session.member.tauMember(), message, reactionType);
            LOGGER.info("Reaction added: {} to message {} by {}", reactionType.name(), message.id(), nickname);
            for (Session s : session.server.getAgents()) {
                if (notReactionReceiver.contains(s)) continue;
                var chain = new ReactionResponseServerChain(s);
                s.io().chainManager.linkChain(chain);
                try {
                    chain.reaction(re, s == session);
                } catch (UnhandledMessageTypeException e) {
                    s.addToCluster(notReactionReceiver);
                }
                s.io().chainManager.removeChain(chain);
            }
        } else {
            if (reactionEntryRepo.delete(message, session.member.tauMember(), reactionType)) {
                for (Session s : session.server.getAgents()) {
                    if (notReactionReceiver.contains(s)) continue;
                    var chain = new ReactionResponseServerChain(s);
                    s.io().chainManager.linkChain(chain);
                    try {
                        chain.unreaction(nickname, message, reactionType, s == session);
                    } catch (UnhandledMessageTypeException e) {
                        s.addToCluster(notReactionReceiver);
                    }
                    s.io().chainManager.removeChain(chain);
                }

                LOGGER.info("Reaction removed: {} from message {}", reactionType.name(), message.id());
            } else {
                throw new NoEffectException();
            }
        }

        return new HappyMessage();
    }
}