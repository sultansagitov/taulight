package net.result.taulight.chain.receiver;

import net.result.sandnode.chain.ReceiverChain;
import net.result.sandnode.chain.ServerChain;
import net.result.sandnode.exception.error.UnauthorizedException;
import net.result.sandnode.message.RawMessage;
import net.result.taulight.dto.ChatInfoDTO;
import net.result.taulight.dto.ChatInfoPropDTO;
import net.result.taulight.dto.ChatMessageViewDTO;
import net.result.taulight.entity.ChatEntity;
import net.result.taulight.entity.DialogEntity;
import net.result.taulight.entity.GroupEntity;
import net.result.taulight.entity.TauMemberEntity;
import net.result.taulight.message.types.ChatRequest;
import net.result.taulight.message.types.ChatResponse;
import net.result.taulight.repository.MessageFileRepository;
import net.result.taulight.repository.MessageRepository;
import net.result.taulight.repository.TauMemberRepository;
import net.result.taulight.util.ChatUtil;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ChatServerChain extends ServerChain implements ReceiverChain {
    private MessageRepository messageRepo;
    private MessageFileRepository messageFileRepo;

    @Override
    public ChatResponse handle(RawMessage raw) {
        TauMemberRepository tauMemberRepo = session.server.container.get(TauMemberRepository.class);
        messageRepo = session.server.container.get(MessageRepository.class);
        messageFileRepo = session.server.container.get(MessageFileRepository.class);

        ChatRequest request = new ChatRequest(raw);

        if (session.member == null) throw new UnauthorizedException();

        TauMemberEntity you = tauMemberRepo.findByMember(session.member);

        Collection<UUID> allChatID = request.dto().allChatID;
        Collection<ChatInfoPropDTO> chatInfoProps = request.dto().infoProps;

        Collection<ChatInfoDTO> infos = (allChatID != null && !allChatID.isEmpty())
                ? byID(allChatID, chatInfoProps, you)
                : byMember(chatInfoProps, you);

        return new ChatResponse(infos);
    }

    private List<ChatInfoDTO> byMember(Collection<ChatInfoPropDTO> props, TauMemberEntity you) {
        List<ChatInfoDTO> infos = new ArrayList<>();
        boolean needLastMessage = props.contains(ChatInfoPropDTO.lastMessage);
        Set<UUID> chatIds = new HashSet<>();
        List<ChatEntity> relevantChats = new ArrayList<>();

        Stream.concat(
                you.getGroups().stream().filter(group -> props.contains(ChatInfoPropDTO.groupID)),
                you.getDialogs().stream().filter(dialog -> props.contains(ChatInfoPropDTO.dialogID))
        ).forEach(group -> {
            relevantChats.add(group);
            if (needLastMessage) chatIds.add(group.id());
        });


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

    private List<ChatInfoDTO> byID(Collection<UUID> chatIDs, Collection<ChatInfoPropDTO> props, TauMemberEntity you) {
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
            boolean groupAccessible = chat instanceof GroupEntity g && g.getMembers().contains(you);
            boolean dialogAccessible = chat instanceof DialogEntity d &&
                    (d.getFirstMember().equals(you) || d.getSecondMember().equals(you));

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

    private Map<UUID, ChatMessageViewDTO> fetchLastMessages(Set<UUID> chatIds, boolean needed) {
        if (!needed || chatIds.isEmpty()) return Collections.emptyMap();

        return messageRepo
                .findLastMessagesByChats(chatIds).stream()
                .collect(Collectors.toMap(m -> m.getChat().id(), m -> m.toViewDTO(messageFileRepo), (a, b) -> a));
    }
}