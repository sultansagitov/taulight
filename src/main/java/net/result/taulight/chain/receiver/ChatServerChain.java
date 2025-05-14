package net.result.taulight.chain.receiver;

import net.result.sandnode.chain.ReceiverChain;
import net.result.sandnode.chain.ServerChain;
import net.result.sandnode.error.Errors;
import net.result.sandnode.exception.DatabaseException;
import net.result.sandnode.exception.DeserializationException;
import net.result.sandnode.exception.UnprocessedMessagesException;
import net.result.sandnode.serverclient.Session;
import net.result.taulight.db.*;
import net.result.taulight.dto.ChatInfoDTO;
import net.result.taulight.dto.ChatInfoPropDTO;
import net.result.taulight.dto.ChatMessageViewDTO;
import net.result.taulight.message.types.ChatRequest;
import net.result.taulight.message.types.ChatResponse;
import net.result.taulight.util.ChatUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.stream.Collectors;

public class ChatServerChain extends ServerChain implements ReceiverChain {
    private static final Logger LOGGER = LogManager.getLogger(ChatServerChain.class);
    private MessageRepository messageRepo;

    public ChatServerChain(Session session) {
        super(session);
    }

    @Override
    public void sync() throws InterruptedException, DeserializationException, UnprocessedMessagesException {
        messageRepo = session.server.container.get(MessageRepository.class);

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

                if (allChatID != null && !allChatID.isEmpty()) {
                    byID(infos, allChatID, chatInfoProps, tauMember);
                } else {
                    byMember(infos, chatInfoProps, tauMember);
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

    private void byMember(
            Collection<ChatInfoDTO> infos,
            Collection<ChatInfoPropDTO> chatInfoProps,
            TauMemberEntity tauMember
    ) throws DatabaseException {
        boolean needLastMessage = chatInfoProps.contains(ChatInfoPropDTO.lastMessage);
        Set<UUID> chatIdsForLastMsg = new HashSet<>();
        List<ChatEntity> relevantChats = new ArrayList<>();

        for (var channel : tauMember.channels()) {
            if (chatInfoProps.contains(ChatInfoPropDTO.channelID)) {
                relevantChats.add(channel);
                if (needLastMessage) chatIdsForLastMsg.add(channel.id());
            }
        }

        for (var dialog : tauMember.dialogs()) {
            if (chatInfoProps.contains(ChatInfoPropDTO.dialogID)) {
                relevantChats.add(dialog);
                if (needLastMessage) chatIdsForLastMsg.add(dialog.id());
            }
        }

        Map<UUID, ChatMessageViewDTO> lastMessages = needLastMessage
                ? messageRepo.findLastMessagesByChats(chatIdsForLastMsg)
                    .stream()
                    .collect(Collectors.toMap(
                            m -> m.chat().id(),
                            ChatMessageViewDTO::new,
                            (a, b) -> a
                    ))
                : Collections.emptyMap();

        for (ChatEntity chat : relevantChats) {
            ChatMessageViewDTO lastMsg = lastMessages.get(chat.id());

            if (chat instanceof ChannelEntity channel) {
                infos.add(ChatInfoDTO.channel(channel, tauMember, chatInfoProps, lastMsg));
            } else if (chat instanceof DialogEntity dialog) {
                infos.add(ChatInfoDTO.dialog(dialog, tauMember, chatInfoProps, lastMsg));
            }
        }
    }

    private void byID(
            Collection<ChatInfoDTO> infos,
            Collection<UUID> allChatID,
            Collection<ChatInfoPropDTO> chatInfoProps,
            TauMemberEntity tauMember
    ) throws DatabaseException {
        ChatUtil chatUtil = session.server.container.get(ChatUtil.class);
        boolean needLastMessage = chatInfoProps.contains(ChatInfoPropDTO.lastMessage);

        Set<UUID> accessibleChatIds = new HashSet<>();
        Map<UUID, ChatEntity> validChats = new HashMap<>();

        for (UUID chatID : allChatID) {
            Optional<ChatEntity> opt = chatUtil.getChat(chatID);
            if (opt.isEmpty()) {
                infos.add(ChatInfoDTO.chatNotFound(chatID));
                continue;
            }

            ChatEntity chat = opt.get();

            boolean accessible = false;

            if (chat instanceof ChannelEntity channel) {
                accessible = channel.members().contains(tauMember);
            } else if (chat instanceof DialogEntity dialog) {
                accessible = dialog.firstMember().equals(tauMember) || dialog.secondMember().equals(tauMember);
            }

            if (accessible) {
                validChats.put(chatID, chat);
                if (needLastMessage) accessibleChatIds.add(chatID);
            } else {
                infos.add(ChatInfoDTO.chatNotFound(chatID));
            }
        }

        Map<UUID, ChatMessageViewDTO> lastMessages = needLastMessage
                ? messageRepo.findLastMessagesByChats(accessibleChatIds)
                .stream()
                .collect(Collectors.toMap(
                        m -> m.chat().id(),
                        ChatMessageViewDTO::new,
                        (a, b) -> a
                ))
                : Collections.emptyMap();

        for (Map.Entry<UUID, ChatEntity> entry : validChats.entrySet()) {
            UUID chatID = entry.getKey();
            ChatEntity chat = entry.getValue();
            ChatMessageViewDTO lastMsg = lastMessages.get(chatID);

            if (chat instanceof ChannelEntity channel && chatInfoProps.contains(ChatInfoPropDTO.channelID)) {
                infos.add(ChatInfoDTO.channel(channel, tauMember, chatInfoProps, lastMsg));
            } else if (chat instanceof DialogEntity dialog && chatInfoProps.contains(ChatInfoPropDTO.dialogID)) {
                infos.add(ChatInfoDTO.dialog(dialog, tauMember, chatInfoProps, lastMsg));
            }
        }
    }
}
