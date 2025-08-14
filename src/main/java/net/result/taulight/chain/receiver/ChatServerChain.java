package net.result.taulight.chain.receiver;

import net.result.sandnode.chain.ReceiverChain;
import net.result.sandnode.chain.ServerChain;
import net.result.sandnode.exception.error.UnauthorizedException;
import net.result.sandnode.message.RawMessage;
import net.result.sandnode.serverclient.Session;
import net.result.sandnode.db.JPAUtil;
import net.result.taulight.dto.ChatInfoDTO;
import net.result.taulight.dto.ChatInfoPropDTO;
import net.result.taulight.dto.ChatMessageViewDTO;
import net.result.taulight.entity.*;
import net.result.taulight.message.types.ChatRequest;
import net.result.taulight.message.types.ChatResponse;
import net.result.taulight.repository.MessageFileRepository;
import net.result.taulight.repository.MessageRepository;
import net.result.taulight.util.ChatUtil;

import java.util.*;

public class ChatServerChain extends ServerChain implements ReceiverChain {
    private MessageRepository messageRepo;
    private MessageFileRepository messageFileRepo;

    public ChatServerChain(Session session) {
        super(session);
    }

    @Override
    public ChatResponse handle(RawMessage raw) throws Exception {
        messageRepo = session.server.container.get(MessageRepository.class);
        messageFileRepo = session.server.container.get(MessageFileRepository.class);
        JPAUtil jpaUtil = session.server.container.get(JPAUtil.class);

        ChatRequest request = new ChatRequest(raw);

        if (session.member == null) throw new UnauthorizedException();

        session.member = jpaUtil.refresh(session.member);

        TauMemberEntity you = session.member.tauMember();

        Collection<UUID> allChatID = request.dto().allChatID;
        Collection<ChatInfoPropDTO> chatInfoProps = request.dto().infoProps;

        Collection<ChatInfoDTO> infos = (allChatID != null && !allChatID.isEmpty())
                ? byID(allChatID, chatInfoProps, you)
                : byMember(chatInfoProps, you);

        return new ChatResponse(infos);
    }

    private List<ChatInfoDTO> byMember(Collection<ChatInfoPropDTO> props, TauMemberEntity you) throws Exception {
        List<ChatInfoDTO> infos = new ArrayList<>();
        boolean needLastMessage = props.contains(ChatInfoPropDTO.lastMessage);
        Set<UUID> chatIds = new HashSet<>();
        List<ChatEntity> relevantChats = new ArrayList<>();

        for (GroupEntity group : you.groups()) {
            if (props.contains(ChatInfoPropDTO.groupID)) {
                relevantChats.add(group);
                if (needLastMessage) chatIds.add(group.id());
            }
        }

        for (DialogEntity dialog : you.dialogs()) {
            if (props.contains(ChatInfoPropDTO.dialogID)) {
                relevantChats.add(dialog);
                if (needLastMessage) chatIds.add(dialog.id());
            }
        }

        Map<UUID, ChatMessageViewDTO> lastMessages = fetchLastMessages(chatIds, needLastMessage);

        for (ChatEntity chat : relevantChats) {
            ChatMessageViewDTO lastMsg = lastMessages.get(chat.id());

            if (chat instanceof GroupEntity group) {
                infos.add(group.toDTO(you, props, lastMsg));
            } else if (chat instanceof DialogEntity dialog) {
                infos.add(dialog.toDTO(you, props, lastMsg));
            }
        }

        return infos;
    }

    private List<ChatInfoDTO> byID(Collection<UUID> chatIDs, Collection<ChatInfoPropDTO> props, TauMemberEntity you)
            throws Exception {

        List<ChatInfoDTO> infos = new ArrayList<>();
        ChatUtil chatUtil = session.server.container.get(ChatUtil.class);
        boolean needLastMessage = props.contains(ChatInfoPropDTO.lastMessage);

        Set<UUID> validChatIds = new HashSet<>();
        Map<UUID, ChatEntity> chats = new HashMap<>();

        for (UUID chatID : chatIDs) {
            Optional<ChatEntity> opt = chatUtil.getChat(chatID);
            if (opt.isEmpty()) {
                infos.add(ChatInfoDTO.chatNotFound(chatID));
                continue;
            }

            ChatEntity chat = opt.get();
            boolean groupAccessible = chat instanceof GroupEntity g && g.members().contains(you);
            boolean dialogAccessible = chat instanceof DialogEntity d &&
                    (d.firstMember().equals(you) || d.secondMember().equals(you));

            if (!groupAccessible && !dialogAccessible) {
                infos.add(ChatInfoDTO.chatNotFound(chatID));
                continue;
            }

            chats.put(chatID, chat);
            if (needLastMessage) validChatIds.add(chatID);
        }

        Map<UUID, ChatMessageViewDTO> lastMessages = fetchLastMessages(validChatIds, needLastMessage);

        for (Map.Entry<UUID, ChatEntity> entry : chats.entrySet()) {
            ChatEntity chat = entry.getValue();
            ChatMessageViewDTO lastMsg = lastMessages.get(entry.getKey());

            if (chat instanceof GroupEntity group && props.contains(ChatInfoPropDTO.groupID)) {
                infos.add(group.toDTO(you, props, lastMsg));
            } else if (chat instanceof DialogEntity dialog && props.contains(ChatInfoPropDTO.dialogID)) {
                infos.add(dialog.toDTO(you, props, lastMsg));
            }
        }

        return infos;
    }

    private Map<UUID, ChatMessageViewDTO> fetchLastMessages(Set<UUID> chatIds, boolean needed) throws Exception {
        if (!needed || chatIds.isEmpty()) return Collections.emptyMap();

        Map<UUID, ChatMessageViewDTO> result = new HashMap<>();
        for (MessageEntity message : messageRepo.findLastMessagesByChats(chatIds)) {
            result.putIfAbsent(message.chat().id(), message.toViewDTO(messageFileRepo));
        }
        return result;
    }
}