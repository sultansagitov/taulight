package net.result.main.commands.taulight;

import net.result.main.commands.CommandInfo;
import net.result.main.commands.CommandRegistry;
import net.result.main.commands.ConsoleContext;
import net.result.taulight.chain.sender.ChatClientChain;
import net.result.taulight.dto.*;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

public class ChatsCommands {

    public static void register(CommandRegistry registry) {
        registry.register(new CommandInfo(":", "Set current chat", "Chats", ChatsCommands::setChat));
        registry.register(new CommandInfo("chats", "List all chats", "Chats", ChatsCommands::chats));
        registry.register(new CommandInfo("dialogs", "List all dialogs", "Chats", ChatsCommands::dialogs));
        registry.register(new CommandInfo("groups", "List all groups", "Chats", ChatsCommands::groups));
        registry.register(new CommandInfo("info", "Show current chat info", "Chats", ChatsCommands::info));
        registry.register(new CommandInfo("newGroup", "Create a new group", "Chats", ChatsCommands::newGroup));
        registry.register(
                new CommandInfo("addMember", "Add a member to the current group", "Chats", ChatsCommands::addMember)
        );
        registry.register(new CommandInfo("leave", "Leave the current group", "Chats", ChatsCommands::leave));
        registry.register(new CommandInfo("dialog", "Start a direct dialog", "Chats", ChatsCommands::dialog));
        registry.register(new CommandInfo(
                "members",
                "List members of the current group",
                "Chats",
                ChatsCommands::members
        ));
        registry.register(new CommandInfo(
                "setGroupAvatar",
                "Set the avatar of the current group",
                "Chats",
                ChatsCommands::setGroupAvatar
        ));
        registry.register(new CommandInfo(
                "getGroupAvatar",
                "Get the avatar of the current group",
                "Chats",
                ChatsCommands::getGroupAvatar
        ));
        registry.register(new CommandInfo(
                "getDialogAvatar",
                "Get a dialog partner's avatar by dialog ID",
                "Chats",
                ChatsCommands::getDialogAvatar
        ));
    }

    private static void setChat(List<String> args, ConsoleContext context) {
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

    private static void chats(List<String> ignored, ConsoleContext context) {
        ChatsRunner.chats(context, ChatInfoPropDTO.all());
    }

    private static void dialogs(List<String> ignored, ConsoleContext context) {
        ChatsRunner.chats(context, ChatInfoPropDTO.dialogAll());
    }

    private static void groups(List<String> ignored, ConsoleContext context) {
        ChatsRunner.chats(context, ChatInfoPropDTO.groupAll());
    }

    private static void info(@NotNull List<String> args, ConsoleContext context) {
        UUID chatID = args.stream().findFirst().map(UUID::fromString).orElse(context.currentChat);

        if (chatID == null) {
            System.out.println("Chat not selected");
            return;
        }

        ChatsRunner.info(context, chatID);
    }

    private static void newGroup(@NotNull List<String> args, ConsoleContext context) {
        if (args.isEmpty()) {
            System.out.println("Usage: newGroup <title>");
            return;
        }

        String title = args.get(0);

        ChatsRunner.newGroup(context, title);
    }

    private static void addMember(@NotNull List<String> args, ConsoleContext context) {
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

        ChatsRunner.addMember(context, chatID, otherNickname, expirationTime);
    }

    private static void dialog(@NotNull List<String> args, ConsoleContext context) {
        if (args.isEmpty()) {
            System.out.println("Usage: dialog <nickname>");
            return;
        }

        String nickname = args.get(0);

        ChatsRunner.dialog(context, nickname);
    }

    private static void leave(@NotNull List<String> args, ConsoleContext context) {
        UUID chatID = args.stream().findFirst().map(UUID::fromString).orElse(context.currentChat);

        if (chatID == null) {
            System.out.println("Chat not selected");
            return;
        }

        ChatsRunner.leave(context, chatID);
    }

    private static void members(@NotNull List<String> args, ConsoleContext context) {
        UUID chatID = args.stream().findFirst().map(UUID::fromString).orElse(context.currentChat);

        if (chatID == null) {
            System.out.println("Chat not selected");
            return;
        }

        MembersResponseDTO response = ChatsRunner.members(context, chatID);
        for (ChatMemberDTO member : response.members) {
            StringBuilder builder = new StringBuilder();
            if (response.roles != null && member.roles != null) {
                builder.append(" - ");
                for (RoleDTO r : response.roles) {
                    if (member.roles.contains(r.id)) {
                        builder.append(r.name);
                        builder.append(", ");
                    }
                }
            }
            System.out.printf("%s - %s%s%n", member.nickname, member.status, builder);
        }
    }

    private static void setGroupAvatar(@NotNull List<String> args, ConsoleContext context) {
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

        ChatsRunner.setGroupAvatar(context, chatID, path);
    }

    private static void getGroupAvatar(@NotNull List<String> args, ConsoleContext context) {
        UUID chatID = args.stream().findFirst().map(UUID::fromString).orElse(context.currentChat);

        if (chatID == null) {
            System.out.println("Chat not selected.");
            return;
        }

        ChatsRunner.getGroupAvatar(context, chatID);
    }

    private static void getDialogAvatar(List<String> args, ConsoleContext context) {

        UUID chatID = args.stream().findFirst().map(UUID::fromString).orElse(context.currentChat);

        if (chatID == null) {
            System.out.println("Chat not selected.");
            return;
        }

        ChatsRunner.getDialogAvatar(context, chatID);
    }
}
