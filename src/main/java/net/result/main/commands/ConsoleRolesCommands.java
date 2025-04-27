package net.result.main.commands;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@SuppressWarnings("SameReturnValue")
public class ConsoleRolesCommands {
    public static void register(Map<String, ConsoleSandnodeCommands.LoopCondition> commands) {
        commands.put("roles", ConsoleRolesCommands::roles);
        commands.put("addRole", ConsoleRolesCommands::addRole);
        commands.put("role", ConsoleRolesCommands::role);
    }

    private static boolean roles(List<String> args, ConsoleContext context) {
        UUID chatID = args.stream().findFirst().map(UUID::fromString).orElse(context.currentChat);
        if (chatID == null) return false;

        ConsoleRolesRunner.roles(context, chatID);
        return false;
    }

    private static boolean addRole(List<String> args, ConsoleContext context) {
        if (args.isEmpty()) {
            System.out.println("Please provide a role name.");
            return false;
        }

        UUID chatID;
        if (args.size() > 1) {
            try {
                chatID = UUID.fromString(args.get(0));
            } catch (IllegalArgumentException e) {
                System.out.println("Invalid UUID format provided for chat ID.");
                return false;
            }
        } else {
            chatID = context.currentChat;
            if (chatID == null) {
                System.out.println("No chat selected or invalid chat ID.");
                return false;
            }
        }

        String roleName = args.get(args.size() - 1);
        if (roleName.isEmpty()) {
            System.out.println("Role name cannot be empty.");
            return false;
        }

        ConsoleRolesRunner.addRole(context, chatID, roleName);
        return false;
    }


    private static boolean role(List<String> args, ConsoleContext context) {
        if (args.size() < 2) {
            System.out.println("Please provide a nickname and role name.");
            return false;
        }

        UUID chatID;
        if (args.size() > 2) {
            try {
                chatID = UUID.fromString(args.get(0));
            } catch (IllegalArgumentException e) {
                System.out.println("Invalid UUID format provided for chat ID.");
                return false;
            }
        } else {
            chatID = context.currentChat;
            if (chatID == null) {
                System.out.println("No chat selected or invalid chat ID.");
                return false;
            }
        }

        String nickname = args.get(args.size() - 2);
        String roleName = args.get(args.size() - 1);

        // Ensure nickname and role name are not empty
        if (nickname.isEmpty() || roleName.isEmpty()) {
            System.out.println("Nickname and role name cannot be empty.");
            return false;
        }

        ConsoleRolesRunner.role(context, chatID, nickname, roleName);
        return false;
    }

}
