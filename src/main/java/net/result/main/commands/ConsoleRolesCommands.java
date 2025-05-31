package net.result.main.commands;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@SuppressWarnings("SameReturnValue")
public class ConsoleRolesCommands {
    public static void register(Map<String, LoopCondition> commands) {
        commands.put("roles", ConsoleRolesCommands::roles);
        commands.put("addRole", ConsoleRolesCommands::addRole);
        commands.put("role", ConsoleRolesCommands::role);
    }

    private static void roles(List<String> args, ConsoleContext context) throws Exception {
        UUID chatID = args.stream().findFirst().map(UUID::fromString).orElse(context.currentChat);
        if (chatID == null) return;

        ConsoleRolesRunner.roles(context, chatID);
    }

    private static void addRole(List<String> args, ConsoleContext context) throws Exception {
        if (args.isEmpty()) {
            System.out.println("Please provide a role name.");
            return;
        }

        UUID chatID;
        if (args.size() > 1) {
            chatID = UUID.fromString(args.get(0));
        } else {
            chatID = context.currentChat;
            if (chatID == null) {
                System.out.println("No chat selected or invalid chat ID.");
                return;
            }
        }

        String roleName = args.get(args.size() - 1);
        if (roleName.isEmpty()) {
            System.out.println("Role name cannot be empty.");
            return;
        }

        ConsoleRolesRunner.addRole(context, chatID, roleName);
    }


    private static void role(List<String> args, ConsoleContext context) throws Exception {
        if (args.size() < 2) {
            System.out.println("Please provide a nickname and role name.");
            return;
        }

        UUID chatID;
        if (args.size() > 2) {
            chatID = UUID.fromString(args.get(0));
        } else {
            chatID = context.currentChat;
            if (chatID == null) {
                System.out.println("No chat selected or invalid chat ID.");
                return;
            }
        }

        String nickname = args.get(args.size() - 2);
        String roleName = args.get(args.size() - 1);

        // Ensure nickname and role name are not empty
        if (nickname.isEmpty() || roleName.isEmpty()) {
            System.out.println("Nickname and role name cannot be empty.");
            return;
        }

        ConsoleRolesRunner.role(context, chatID, nickname, roleName);
    }
}
