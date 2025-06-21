package net.result.main.commands;

import net.result.taulight.db.Permission;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ConsoleGroupPermissionsCommands {
    public static void register(Map<String, LoopCondition> commands) {
        commands.put("addDefaultPerm", ConsoleGroupPermissionsCommands::addDefaultPerm);
        commands.put("removeDefaultPerm", ConsoleGroupPermissionsCommands::removeDefaultPerm);
        commands.put("addRolePerm", ConsoleGroupPermissionsCommands::addRolePerm);
        commands.put("removeRolePerm", ConsoleGroupPermissionsCommands::removeRolePerm);
    }

    private static void addDefaultPerm(@NotNull List<String> args, ConsoleContext context) throws Exception {
        if (args.isEmpty()) {
            System.out.println("Usage: addDefaultPerm [chatID] <permission>");
            return;
        }

        UUID chatID;
        Permission permission;

        if (looksLikeUUID(args.get(0)) && args.size() >= 2) {
            chatID = UUID.fromString(args.get(0));
            permission = Permission.valueOf(args.get(1).toUpperCase());
        } else {
            chatID = context.currentChat;
            permission = Permission.valueOf(args.get(0).toUpperCase());
        }

        if (chatID == null) {
            System.out.println("No chat selected or provided.");
            return;
        }

        ConsoleGroupPermissionsRunner.addDefaultPerm(context, chatID, permission);
    }

    private static void removeDefaultPerm(@NotNull List<String> args, ConsoleContext context) throws Exception {
        if (args.isEmpty()) {
            System.out.println("Usage: removeDefaultPerm [chatID] <permission>");
            return;
        }

        UUID chatID;
        Permission permission;

        if (looksLikeUUID(args.get(0)) && args.size() >= 2) {
            chatID = UUID.fromString(args.get(0));
            permission = Permission.valueOf(args.get(1).toUpperCase());
        } else {
            chatID = context.currentChat;
            permission = Permission.valueOf(args.get(0).toUpperCase());
        }

        if (chatID == null) {
            System.out.println("No chat selected or provided.");
            return;
        }

        ConsoleGroupPermissionsRunner.removeDefaultPerm(context, chatID, permission);
    }

    private static void addRolePerm(@NotNull List<String> args, ConsoleContext context) throws Exception {
        if (args.size() < 2) {
            System.out.println("Usage: addRolePerm [chatID] <roleID> <permission>");
            return;
        }

        UUID chatID;
        UUID roleID;
        Permission permission;

        if (looksLikeUUID(args.get(0)) && args.size() >= 3) {
            chatID = UUID.fromString(args.get(0));
            roleID = UUID.fromString(args.get(1));
            permission = Permission.valueOf(args.get(2).toUpperCase());
        } else {
            chatID = context.currentChat;
            roleID = UUID.fromString(args.get(0));
            permission = Permission.valueOf(args.get(1).toUpperCase());
        }

        if (chatID == null) {
            System.out.println("No chat selected or provided.");
            return;
        }

        ConsoleGroupPermissionsRunner.addRolePerm(context, chatID, roleID, permission);
    }

    private static void removeRolePerm(@NotNull List<String> args, ConsoleContext context) throws Exception {
        if (args.size() < 2) {
            System.out.println("Usage: removeRolePerm [chatID] <roleID> <permission>");
            return;
        }

        UUID chatID;
        UUID roleID;
        Permission permission;

        if (looksLikeUUID(args.get(0)) && args.size() >= 3) {
            chatID = UUID.fromString(args.get(0));
            roleID = UUID.fromString(args.get(1));
            permission = Permission.valueOf(args.get(2).toUpperCase());
        } else {
            chatID = context.currentChat;
            roleID = UUID.fromString(args.get(0));
            permission = Permission.valueOf(args.get(1).toUpperCase());
        }

        if (chatID == null) {
            System.out.println("No chat selected or provided.");
            return;
        }

        ConsoleGroupPermissionsRunner.removeRolePerm(context, chatID, roleID, permission);
    }

    private static boolean looksLikeUUID(String value) {
        try {
            UUID.fromString(value);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
