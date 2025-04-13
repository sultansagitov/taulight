package net.result.taulight.chain.receiver;

import net.result.sandnode.chain.ReceiverChain;
import net.result.sandnode.chain.receiver.ServerChain;
import net.result.sandnode.error.Errors;
import net.result.sandnode.exception.DatabaseException;
import net.result.sandnode.exception.DeserializationException;
import net.result.sandnode.exception.UnprocessedMessagesException;
import net.result.sandnode.serverclient.Session;
import net.result.taulight.db.*;
import net.result.taulight.dto.ChatInfoDTO;
import net.result.taulight.dto.ChatInfoPropDTO;
import net.result.taulight.message.types.ChatRequest;
import net.result.taulight.message.types.ChatResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

public class ChatServerChain extends ServerChain implements ReceiverChain {
    private static final Logger LOGGER = LogManager.getLogger(ChatServerChain.class);

    public ChatServerChain(Session session) {
        super(session);
    }

    @Override
    public void sync() throws InterruptedException, DeserializationException, UnprocessedMessagesException {
        TauDatabase database = (TauDatabase) session.server.serverConfig.database();
        while (true) {
            ChatRequest request = new ChatRequest(queue.take());

            if (session.member == null) {
                send(Errors.UNAUTHORIZED.createMessage());
                continue;
            }

            TauMemberEntity tauMember = session.member.tauMember();

            Collection<UUID> allChatID = request.getAllChatID();
            Collection<ChatInfoPropDTO> chatInfoProps = request.getChatInfoProps();

            try {
                Collection<ChatInfoDTO> infos = new ArrayList<>();

                if (allChatID == null || allChatID.isEmpty()) {
                    LOGGER.debug("channels {}", tauMember.channels());
                    for (var channel : tauMember.channels()) {
                        if (!Collections.disjoint(chatInfoProps, ChatInfoPropDTO.channelAll())) {
                            infos.add(ChatInfoDTO.channel(channel, tauMember, chatInfoProps));
                        }
                    }

                    LOGGER.debug("dialogs {}", tauMember.dialogs());
                    for (var dialog : tauMember.dialogs()) {
                        if (!Collections.disjoint(chatInfoProps, ChatInfoPropDTO.dialogAll())) {
                            infos.add(ChatInfoDTO.dialog(dialog, tauMember, chatInfoProps));
                        }
                    }
                } else {
                    for (UUID chatID : allChatID) {
                        Optional<ChatEntity> opt = database.getChat(chatID);
                        if (opt.isEmpty()) {
                            infos.add(ChatInfoDTO.chatNotFound(chatID));
                            continue;
                        }

                        ChatEntity chat = opt.get();

                        if (chat instanceof ChannelEntity channel) {
                            if (!channel.members().contains(tauMember)) {
                                infos.add(ChatInfoDTO.chatNotFound(chatID));
                                continue;
                            }

                            if (!Collections.disjoint(chatInfoProps, ChatInfoPropDTO.channelAll())) {
                                infos.add(ChatInfoDTO.channel(channel, tauMember, chatInfoProps));
                            }
                        }

                        if (!(chat instanceof DialogEntity dialog)) continue;

                        if (dialog.firstMember() != tauMember && dialog.secondMember() != tauMember) {
                            infos.add(ChatInfoDTO.chatNotFound(chatID));
                        } else if (!Collections.disjoint(chatInfoProps, ChatInfoPropDTO.dialogAll())) {
                            infos.add(ChatInfoDTO.dialog(dialog, tauMember, chatInfoProps));
                        }
                    }
                }

                send(new ChatResponse(infos));
            } catch (DatabaseException e) {
                LOGGER.error(e);
                send(Errors.SERVER_ERROR.createMessage());
                continue;
            }

            if (request.headers().fin()) break;
        }
    }
}
