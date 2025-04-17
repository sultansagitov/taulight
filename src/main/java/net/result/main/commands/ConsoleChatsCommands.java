package net.result.main.commands;

import net.result.sandnode.exception.*;
import net.result.taulight.dto.ChatInfoPropDTO;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.*;

@SuppressWarnings("SameReturnValue")
public class ConsoleChatsCommands {
    public static void register(Map<String, ConsoleSandnodeCommands.LoopCondition> commands) {
        commands.put(":", ConsoleChatsCommands::setChat);
        commands.put("chats", ConsoleChatsCommands::chats);
        commands.put("dialogs", ConsoleChatsCommands::dialogs);
        commands.put("channels", ConsoleChatsCommands::channels);
        commands.put("info", ConsoleChatsCommands::info);
        commands.put("newChannel", ConsoleChatsCommands::newChannel);
        commands.put("addMember", ConsoleChatsCommands::addMember);
        commands.put("leave", ConsoleChatsCommands::leave);
        commands.put("dialog", ConsoleChatsCommands::dialog);
        commands.put("members", ConsoleChatsCommands::members);
    }

    private static boolean setChat(List<String> args, ConsoleContext context) {
        try {
            context.currentChat = UUID.fromString(args.get(0));
        } catch (IllegalArgumentException e) {
            System.out.println("Invalid UUID format provided.");
        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.println("No arguments provided. Expected a UUID.");
        }

        return false;
    }

    private static boolean chats(List<String> ignored, ConsoleContext context)
            throws InterruptedException, UnprocessedMessagesException {
        ConsoleChatsRunner.chats(context, ChatInfoPropDTO.all());
        return false;
    }

    private static boolean dialogs(List<String> ignored, ConsoleContext context)
            throws InterruptedException, UnprocessedMessagesException {
        ConsoleChatsRunner.chats(context, ChatInfoPropDTO.dialogAll());
        return false;
    }

    private static boolean channels(List<String> ignored, ConsoleContext context)
            throws InterruptedException, UnprocessedMessagesException {
        ConsoleChatsRunner.chats(context, ChatInfoPropDTO.channelAll());
        return false;
    }

    private static boolean info(@NotNull List<String> args, ConsoleContext context) throws InterruptedException {
        UUID chatID;
        try {
            chatID = args.stream().findFirst().map(UUID::fromString).orElse(context.currentChat);
        } catch (IllegalArgumentException e) {
            System.out.println("Invalid UUID format provided.");
            return false;
        }
        if (chatID == null) {
            System.out.println("Chat not selected");
            return false;
        }
        ConsoleChatsRunner.info(context, chatID);
        return false;
    }

    private static boolean newChannel(@NotNull List<String> args, ConsoleContext context)
            throws InterruptedException, UnprocessedMessagesException {
        if (args.isEmpty()) {
            System.out.println("Usage: newChannel <title>");
            return false;
        }

        String title;

        try {
            title = args.get(0);
        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.println("Missing channel title.");
            return false;
        }

        ConsoleChatsRunner.newChannel(context, title);

        return false;
    }

    private static boolean addMember(@NotNull List<String> args, ConsoleContext context)
            throws InterruptedException, UnprocessedMessagesException {
        UUID chatID = context.currentChat;
        String otherNickname = null;
        Duration expirationTime = Duration.ofHours(24);

        try {
            for (String s : args) {
                String[] split = s.split("=");
                switch (split[0].charAt(0)) {
                    case 'c' -> {
                        try {
                            chatID = UUID.fromString(split[1]);
                        } catch (IllegalArgumentException e) {
                            System.out.println("Invalid UUID format provided.");
                            return false;
                        }
                    }
                    case 'n' -> otherNickname = split[1];
                    case 'e' -> expirationTime = Duration.ofSeconds(Long.parseUnsignedLong(split[1]));
                }
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.println("Invalid format provided.");
            return false;
        } catch (IllegalArgumentException e) {
            System.out.println("Invalid argument format: " + e.getMessage());
            return false;
        }

        if (chatID == null) {
            System.out.println("Chat not selected, use c=<chat>");
            return false;
        }

        if (otherNickname == null) {
            System.out.println("Member not set, use n=<member>");
            return false;
        }

        ConsoleChatsRunner.addMember(context, chatID, otherNickname, expirationTime);

        return false;
    }

    private static boolean dialog(@NotNull List<String> args, ConsoleContext context)
            throws InterruptedException, UnprocessedMessagesException {
        if (args.isEmpty()) {
            System.out.println("Usage: dialog <nickname>");
            return false;
        }

        String nickname;
        try {
            nickname = args.get(0);
        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.println("No nickname provided.");
            return false;
        }

        ConsoleChatsRunner.dialog(context, nickname);

        return false;
    }

    private static boolean leave(@NotNull List<String> args, ConsoleContext context)
            throws InterruptedException, UnprocessedMessagesException {
        UUID chatID;

        try {
            chatID = args.stream().findFirst().map(UUID::fromString).orElse(context.currentChat);
        } catch (IllegalArgumentException e) {
            System.out.println("Invalid UUID format provided: " + e.getMessage());
            return false;
        }

        if (chatID == null) {
            System.out.println("Chat not selected");
            return false;
        }

        ConsoleChatsRunner.leave(context, chatID);

        return false;
    }

    private static boolean members(@NotNull List<String> args, ConsoleContext context)
            throws UnprocessedMessagesException, InterruptedException {
        UUID chatID;

        try {
            chatID = args.stream().findFirst().map(UUID::fromString).orElse(context.currentChat);
        } catch (IllegalArgumentException e) {
            System.out.println("Invalid UUID format provided.");
            return false;
        }

        if (chatID == null) {
            System.out.println("Chat not selected");
            return false;
        }

        ConsoleChatsRunner.members(context, chatID);

        return false;
    }

}
