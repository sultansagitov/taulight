package net.result.main.commands.taulight;

import net.result.main.commands.CommandInfo;
import net.result.main.commands.CommandRegistry;
import net.result.main.commands.ConsoleContext;
import net.result.taulight.dto.*;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.List;
import java.util.UUID;

public class GroupsCommands {

    public static void register(CommandRegistry registry) {
        registry.register(new CommandInfo("newGroup", "Create a new group", "Groups", GroupsCommands::newGroup));
        registry.register(
                new CommandInfo("addMember", "Add a member to the current group", "Groups", GroupsCommands::addMember)
        );
        registry.register(new CommandInfo("leave", "Leave the current group", "Groups", GroupsCommands::leave));
        registry.register(new CommandInfo(
                "members",
                "List members of the current group",
                "Groups",
                GroupsCommands::members
        ));
        registry.register(new CommandInfo(
                "setGroupAvatar",
                "Set the avatar of the current group",
                "Groups",
                GroupsCommands::setGroupAvatar
        ));
        registry.register(new CommandInfo(
                "getGroupAvatar",
                "Get the avatar of the current group",
                "Groups",
                GroupsCommands::getGroupAvatar
        ));
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
}
