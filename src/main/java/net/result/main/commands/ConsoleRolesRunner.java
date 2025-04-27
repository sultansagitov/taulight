package net.result.main.commands;

import net.result.sandnode.exception.DeserializationException;
import net.result.sandnode.exception.ExpectedMessageException;
import net.result.sandnode.exception.UnknownSandnodeErrorException;
import net.result.sandnode.exception.UnprocessedMessagesException;
import net.result.sandnode.exception.error.SandnodeErrorException;
import net.result.sandnode.util.IOController;
import net.result.taulight.chain.sender.RoleClientChain;
import net.result.taulight.dto.RolesDTO;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class ConsoleRolesRunner {

    public static void roles(@NotNull ConsoleContext context, UUID chatID) {
        IOController io = context.io;
        RoleClientChain chain = new RoleClientChain(io);
        io.chainManager.linkChain(chain);

        RolesDTO roles;
        try {
            roles = chain.getRoles(chatID);
        } catch (UnprocessedMessagesException | InterruptedException | UnknownSandnodeErrorException |
                 SandnodeErrorException | DeserializationException | ExpectedMessageException e) {
            System.out.printf("Error fetching roles for chat %s: %s%n", chatID, e.getClass().getSimpleName());
            return;
        } finally {
            io.chainManager.removeChain(chain);
        }

        System.out.printf("Roles in chat %s: Member Roles: %s, All Roles: %s%n",
                chatID, roles.memberRoles, roles.allRoles);
    }

    public static void addRole(ConsoleContext context, UUID chatID, String roleName) {
        if (roleName == null || roleName.isEmpty()) {
            System.out.println("Role name cannot be empty.");
            return;
        }

        IOController io = context.io;
        RoleClientChain chain = new RoleClientChain(io);
        io.chainManager.linkChain(chain);

        try {
            chain.addRole(chatID, roleName);
            System.out.printf("Role '%s' added successfully to chat %s.%n", roleName, chatID);
        } catch (UnprocessedMessagesException | InterruptedException | UnknownSandnodeErrorException |
                 SandnodeErrorException | DeserializationException | ExpectedMessageException e) {
            System.out.printf("Error adding role '%s' to chat %s: %s%n",
                    roleName, chatID, e.getClass().getSimpleName());
        } finally {
            io.chainManager.removeChain(chain);
        }
    }

    public static void role(ConsoleContext context, UUID chatID, String nickname, String roleName) {
        if (nickname == null || nickname.isEmpty() || roleName == null || roleName.isEmpty()) {
            System.out.println("Both nickname and role name must be provided.");
            return;
        }

        IOController io = context.io;
        RoleClientChain chain = new RoleClientChain(io);
        io.chainManager.linkChain(chain);

        try {
            chain.assignRole(chatID, nickname, roleName);
            System.out.printf("Role '%s' successfully assigned to '%s' in chat %s.%n", roleName, nickname, chatID);
        } catch (UnprocessedMessagesException | InterruptedException | UnknownSandnodeErrorException |
                 SandnodeErrorException | DeserializationException | ExpectedMessageException e) {
            System.out.printf("Error assigning role '%s' to user '%s' in chat %s: %s%n",
                    roleName, nickname, chatID, e.getClass().getSimpleName());
        } finally {
            io.chainManager.removeChain(chain);
        }
    }
}
