package net.result.main.commands.taulight;

import net.result.main.commands.CommandInfo;
import net.result.main.commands.CommandRegistry;
import net.result.main.commands.ConsoleContext;
import net.result.taulight.db.Permission;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

public class GroupPermissionsCommands {

    public static void register(CommandRegistry registry) {
        registry.register(new CommandInfo(
                "addDefaultPerm",
                "Add a default permission to the group",
                "Group Permissions",
                GroupPermissionsCommands::addDefaultPerm
        ));
        registry.register(new CommandInfo(
                "removeDefaultPerm",
                "Remove a default permission from the group",
                "Group Permissions",
                GroupPermissionsCommands::removeDefaultPerm
        ));
        registry.register(new CommandInfo(
                "addRolePerm",
                "Add a permission to a specific role in the group",
                "Group Permissions",
                GroupPermissionsCommands::addRolePerm
        ));
        registry.register(new CommandInfo(
                "removeRolePerm",
                "Remove a permission from a specific role in the group",
                "Group Permissions",
                GroupPermissionsCommands::removeRolePerm
        ));
    }


    private static void addDefaultPerm(@NotNull List<String> args, ConsoleContext context) {
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

        GroupPermissionsRunner.addDefaultPerm(context, chatID, permission);
    }

    private static void removeDefaultPerm(@NotNull List<String> args, ConsoleContext context) {
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

        GroupPermissionsRunner.removeDefaultPerm(context, chatID, permission);
    }

    private static void addRolePerm(@NotNull List<String> args, ConsoleContext context) {
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

        GroupPermissionsRunner.addRolePerm(context, chatID, roleID, permission);
    }

    private static void removeRolePerm(@NotNull List<String> args, ConsoleContext context) {
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

        GroupPermissionsRunner.removeRolePerm(context, chatID, roleID, permission);
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
