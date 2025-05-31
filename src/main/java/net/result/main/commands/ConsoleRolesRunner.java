package net.result.main.commands;

import net.result.sandnode.util.IOController;
import net.result.taulight.chain.sender.RoleClientChain;
import net.result.taulight.dto.RolesDTO;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class ConsoleRolesRunner {

    public static void roles(@NotNull ConsoleContext context, UUID chatID) throws Exception {
        IOController io = context.io;
        RoleClientChain chain = new RoleClientChain(context.client);
        io.chainManager.linkChain(chain);
        RolesDTO roles = chain.getRoles(chatID);
        io.chainManager.removeChain(chain);

        System.out.printf("Roles in chat %s: Member Roles: %s, All Roles: %s%n",
                chatID, roles.memberRoles, roles.allRoles);
    }

    public static void addRole(ConsoleContext context, UUID chatID, String roleName) throws Exception {
        if (roleName == null || roleName.isEmpty()) {
            System.out.println("Role name cannot be empty.");
            return;
        }

        IOController io = context.io;
        RoleClientChain chain = new RoleClientChain(context.client);
        io.chainManager.linkChain(chain);
        chain.addRole(chatID, roleName);
        io.chainManager.removeChain(chain);

        System.out.printf("Role '%s' added successfully to chat %s.%n", roleName, chatID);
    }

    public static void role(ConsoleContext context, UUID chatID, String nickname, String roleName)
            throws Exception {
        if (nickname == null || nickname.isEmpty() || roleName == null || roleName.isEmpty()) {
            System.out.println("Both nickname and role name must be provided.");
            return;
        }

        IOController io = context.io;
        RoleClientChain chain = new RoleClientChain(context.client);
        io.chainManager.linkChain(chain);
        chain.assignRole(chatID, nickname, roleName);
        io.chainManager.removeChain(chain);

        System.out.printf("Role '%s' successfully assigned to '%s' in chat %s.%n", roleName, nickname, chatID);
    }
}
