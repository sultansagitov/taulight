package net.result.main.commands.taulight;

import net.result.main.commands.CommandInfo;
import net.result.main.commands.CommandRegistry;
import net.result.main.commands.ConsoleContext;

import java.util.List;
import java.util.UUID;

@SuppressWarnings("SameReturnValue")
public class RolesCommands {

    public static void register(CommandRegistry registry) {
        registry.register(new CommandInfo("roles", "List all roles", "Roles", RolesCommands::roles));
        registry.register(new CommandInfo("addRole", "Add a role to a user", "Roles", RolesCommands::addRole));
        registry.register(new CommandInfo("assignRole", "Assign a role to a user", "Roles", RolesCommands::assignRole));
    }

    private static void roles(List<String> args, ConsoleContext context) {
        UUID chatID = args.stream().findFirst().map(UUID::fromString).orElse(context.currentChat);
        if (chatID == null) return;

        RolesRunner.roles(context, chatID);
    }

    private static void addRole(List<String> args, ConsoleContext context) {
        if (args.isEmpty()) {
            System.out.println("Please provide a role id.");
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

        RolesRunner.addRole(context, chatID, roleName);
    }

    private static void assignRole(List<String> args, ConsoleContext context) {
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
        UUID roleID = UUID.fromString(args.get(args.size() - 1));

        // Ensure nickname and role name are not empty
        if (nickname.isEmpty()) {
            System.out.println("Nickname and role name cannot be empty.");
            return;
        }

        RolesRunner.assignRole(context, chatID, nickname, roleID);
    }
}
