package net.result.taulight.chain.receiver;

import net.result.sandnode.chain.ReceiverChain;
import net.result.sandnode.chain.receiver.ServerChain;
import net.result.sandnode.error.Errors;
import net.result.sandnode.exception.DatabaseException;
import net.result.sandnode.exception.DeserializationException;
import net.result.sandnode.exception.UnprocessedMessagesException;
import net.result.sandnode.serverclient.Session;
import net.result.taulight.db.ChannelEntity;
import net.result.taulight.db.ChatEntity;
import net.result.taulight.db.DialogEntity;
import net.result.taulight.db.TauDatabase;
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

            Collection<UUID> allChatID = request.getAllChatID();
            Collection<ChatInfoPropDTO> chatInfoProps = request.getChatInfoProps();

            try {
                Collection<ChatInfoDTO> infos = new ArrayList<>();

                if (allChatID == null || allChatID.isEmpty()) {
                    for (var channel : session.member.channels()) {
                        if (!Collections.disjoint(chatInfoProps, ChatInfoPropDTO.channelAll())) {
                            infos.add(ChatInfoDTO.channel(channel, session.member, chatInfoProps));
                        }
                    }

                    for (var dialog : session.member.dialogs()) {
                        if (!Collections.disjoint(chatInfoProps, ChatInfoPropDTO.dialogAll())) {
                            infos.add(ChatInfoDTO.dialog(dialog, session.member, chatInfoProps));
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
                            if (!channel.members().contains(session.member)) {
                                infos.add(ChatInfoDTO.chatNotFound(chatID));
                                continue;
                            }

                            if (!Collections.disjoint(chatInfoProps, ChatInfoPropDTO.channelAll())) {
                                infos.add(ChatInfoDTO.channel(channel, session.member, chatInfoProps));
                            }
                        }

                        if (chat instanceof DialogEntity dialog) {
                            if (!dialog.firstMember().equals(session.member)) {
                                if (!dialog.secondMember().equals(session.member)) {
                                    infos.add(ChatInfoDTO.chatNotFound(chatID));
                                    continue;
                                }
                            }

                            if (!Collections.disjoint(chatInfoProps, ChatInfoPropDTO.dialogAll())) {
                                infos.add(ChatInfoDTO.dialog(dialog, session.member, chatInfoProps));
                            }
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
