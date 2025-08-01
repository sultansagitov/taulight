package net.result.main.commands;

import org.jetbrains.annotations.NotNull;

import java.util.*;

@SuppressWarnings("SameReturnValue")
public class ConsoleMessagesCommands {
    public static void register(Map<String, LoopCondition> commands) {
        commands.put("messages", ConsoleMessagesCommands::messages);
        commands.put("reply", ConsoleMessagesCommands::reply);
        commands.put("uploadFile", ConsoleMessagesCommands::uploadFile);
        commands.put("fileAttached", ConsoleMessagesCommands::fileAttached);
        commands.put("downloadFile", ConsoleMessagesCommands::downloadFile);
    }

    private static void messages(@NotNull List<String> args, ConsoleContext context) throws Exception {
        UUID chatID = args.stream().findFirst().map(UUID::fromString).orElse(context.currentChat);

        if (chatID == null) {
            System.out.println("Chat not select");
            return;
        }

        ConsoleMessagesRunner.messages(context, chatID);
    }

    private static void reply(List<String> args, ConsoleContext context) throws Exception {
        if (context.currentChat == null) {
            System.out.println("chat not selected");
            return;
        }

        if (args.isEmpty()) {
            return;
        }

        String firstArg = args.get(0);
        int replyCount = Integer.parseInt(firstArg);

        Set<UUID> replies = new HashSet<>();

        for (int i = 1; i <= replyCount && i < args.size(); i++) {
            replies.add(UUID.fromString(args.get(i)));
        }

        String input = String.join(" ", args.subList(replyCount + 1, args.size()));

        if (input.isEmpty()) {
            System.out.println("Message content is empty");
            return;
        }

        ConsoleMessagesRunner.reply(context, input, replies);
    }

    private static void uploadFile(List<String> args, ConsoleContext context) throws Exception {
        UUID chatID = context.currentChat;
        String path;

        if (chatID == null) {
            chatID = UUID.fromString(args.get(0));
            path = args.get(1);
        } else {
            path = args.get(0);
        }

        UUID fileID = ConsoleMessagesRunner.uploadFile(context, chatID, path);

        System.out.printf("File ID: %s%n", fileID);
    }

    private static void fileAttached(List<String> args, ConsoleContext context) throws Exception {
        if (context.currentChat == null) {
            System.out.println("chat not selected");
            return;
        }

        if (args.size() < 2) {
            System.out.println("Usage: fileAttached <file count> <file UUIDs...> <text input>");
            return;
        }

        int fileCount = Integer.parseInt(args.get(0));
        if (fileCount <= 0 || fileCount >= args.size()) {
            System.out.println("Invalid file count or arguments");
            return;
        }

        Set<UUID> fileIDs = new HashSet<>();
        for (int i = 1; i <= fileCount && i < args.size(); i++) {
            fileIDs.add(UUID.fromString(args.get(i)));
        }

        String input = String.join(" ", args.subList(fileCount + 1, args.size()));

        if (input.isEmpty()) {
            System.out.println("Message content is empty");
            return;
        }

        ConsoleMessagesRunner.fileAttached(context, input, fileIDs);
    }

    private static void downloadFile(List<String> args, ConsoleContext context) throws Exception {
        UUID fileID = UUID.fromString(args.get(0));
        ConsoleMessagesRunner.downloadFile(context, fileID);
    }
}
