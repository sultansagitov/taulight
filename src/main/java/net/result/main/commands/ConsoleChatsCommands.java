package net.result.main.commands;

import net.result.taulight.chain.sender.ChatClientChain;
import net.result.taulight.dto.*;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@SuppressWarnings("SameReturnValue")
public class ConsoleChatsCommands {
    public static void register(Map<String, LoopCondition> commands) {
        commands.put(":", ConsoleChatsCommands::setChat);
        commands.put("chats", ConsoleChatsCommands::chats);
        commands.put("dialogs", ConsoleChatsCommands::dialogs);
        commands.put("groups", ConsoleChatsCommands::groups);
        commands.put("info", ConsoleChatsCommands::info);
        commands.put("newGroup", ConsoleChatsCommands::newGroup);
        commands.put("addMember", ConsoleChatsCommands::addMember);
        commands.put("leave", ConsoleChatsCommands::leave);
        commands.put("dialog", ConsoleChatsCommands::dialog);
        commands.put("members", ConsoleChatsCommands::members);
        commands.put("setGroupAvatar", ConsoleChatsCommands::setGroupAvatar);
        commands.put("getGroupAvatar", ConsoleChatsCommands::getGroupAvatar);
        commands.put("getDialogAvatar", ConsoleChatsCommands::getDialogAvatar);
    }

    private static void setChat(List<String> args, ConsoleContext context) throws Exception {
        UUID currentChat = UUID.fromString(args.get(0));

        ChatClientChain chain = new ChatClientChain(context.client);
        context.io.chainManager.linkChain(chain);
        List<ChatInfoPropDTO> props = List.of(
                ChatInfoPropDTO.groupID,
                ChatInfoPropDTO.dialogID,
                ChatInfoPropDTO.groupTitle,
                ChatInfoPropDTO.dialogOther
        );
        ChatInfoDTO chatInfoDTO = chain.getByID(List.of(currentChat), props).stream().findFirst().orElse(null);
        context.io.chainManager.removeChain(chain);

        context.chat = chatInfoDTO;
        context.currentChat = currentChat;
    }

    private static void chats(List<String> ignored, ConsoleContext context) throws Exception {
        ConsoleChatsRunner.chats(context, ChatInfoPropDTO.all());
    }

    private static void dialogs(List<String> ignored, ConsoleContext context) throws Exception {
        ConsoleChatsRunner.chats(context, ChatInfoPropDTO.dialogAll());
    }

    private static void groups(List<String> ignored, ConsoleContext context) throws Exception {
        ConsoleChatsRunner.chats(context, ChatInfoPropDTO.groupAll());
    }

    private static void info(@NotNull List<String> args, ConsoleContext context) throws Exception {
        UUID chatID = args.stream().findFirst().map(UUID::fromString).orElse(context.currentChat);

        if (chatID == null) {
            System.out.println("Chat not selected");
            return;
        }

        ConsoleChatsRunner.info(context, chatID);
    }

    private static void newGroup(@NotNull List<String> args, ConsoleContext context) throws Exception {
        if (args.isEmpty()) {
            System.out.println("Usage: newGroup <title>");
            return;
        }

        String title = args.get(0);

        ConsoleChatsRunner.newGroup(context, title);
    }

    private static void addMember(@NotNull List<String> args, ConsoleContext context) throws Exception {
        UUID chatID = context.currentChat;
        String otherNickname = null;
        Duration expirationTime = Duration.ofHours(24);

        for (String s : args) {
            String[] split = s.split("=");
            switch (split[0].charAt(0)) {
                case 'c' -> chatID = UUID.fromString(split[1]);
                case 'n' -> otherNickname = split[1];
                case 'e' -> expirationTime = Duration.ofSeconds(Long.parseUnsignedLong(split[1]));
            }
        }

        if (chatID == null) {
            System.out.println("Chat not selected, use c=<chat>");
            return;
        }

        if (otherNickname == null) {
            System.out.println("Member not set, use n=<member>");
            return;
        }

        ConsoleChatsRunner.addMember(context, chatID, otherNickname, expirationTime);
    }

    private static void dialog(@NotNull List<String> args, ConsoleContext context) throws Exception {
        if (args.isEmpty()) {
            System.out.println("Usage: dialog <nickname>");
            return;
        }

        String nickname = args.get(0);

        ConsoleChatsRunner.dialog(context, nickname);
    }

    private static void leave(@NotNull List<String> args, ConsoleContext context) throws Exception {
        UUID chatID = args.stream().findFirst().map(UUID::fromString).orElse(context.currentChat);

        if (chatID == null) {
            System.out.println("Chat not selected");
            return;
        }

        ConsoleChatsRunner.leave(context, chatID);
    }

    private static void members(@NotNull List<String> args, ConsoleContext context) throws Exception {
        UUID chatID = args.stream().findFirst().map(UUID::fromString).orElse(context.currentChat);

        if (chatID == null) {
            System.out.println("Chat not selected");
            return;
        }

        MembersResponseDTO response = ConsoleChatsRunner.members(context, chatID);
        for (ChatMemberDTO member : response.members) {
            StringBuilder builder = new StringBuilder();
            if (response.roles != null && member.roles != null) {
                builder.append(" - ");
                for (RoleDTO r : response.roles) {
                    if (member.roles.contains(r.id.toString())) {
                        builder.append(r.name);
                        builder.append(", ");
                    }
                }
            }
            System.out.printf("%s - %s%s%n", member.nickname, member.status, builder);
        }
    }

    private static void setGroupAvatar(@NotNull List<String> args, ConsoleContext context) throws Exception {
        UUID chatID;
        String path;
        if (args.size() > 1) {
            chatID = UUID.fromString(args.get(0));
            path = args.get(1);
        } else {
            chatID = context.currentChat;
            path = args.get(0);
        }

        if (chatID == null) {
            System.out.println("Chat not selected.");
            return;
        }

        ConsoleChatsRunner.setGroupAvatar(context, chatID, path);
    }

    private static void getGroupAvatar(@NotNull List<String> args, ConsoleContext context) throws Exception {
        UUID chatID = args.stream().findFirst().map(UUID::fromString).orElse(context.currentChat);

        if (chatID == null) {
            System.out.println("Chat not selected.");
            return;
        }

        ConsoleChatsRunner.getGroupAvatar(context, chatID);
    }

    private static void getDialogAvatar(List<String> args, ConsoleContext context) throws Exception {

        UUID chatID = args.stream().findFirst().map(UUID::fromString).orElse(context.currentChat);

        if (chatID == null) {
            System.out.println("Chat not selected.");
            return;
        }

        ConsoleChatsRunner.getDialogAvatar(context, chatID);
    }
}
