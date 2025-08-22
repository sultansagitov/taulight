package net.result.main.commands.taulight;

import net.result.main.commands.ConsoleContext;
import net.result.taulight.chain.sender.PermissionClientChain;
import net.result.taulight.db.Permission;

import java.util.UUID;

public class GroupPermissionsRunner {

    public static void addDefaultPerm(ConsoleContext context, UUID chatID, Permission permission) {
        PermissionClientChain chain = new PermissionClientChain(context.client);
        context.io.chainManager.linkChain(chain);
        chain.addDefault(chatID, permission);
        context.io.chainManager.removeChain(chain);
    }

    public static void removeDefaultPerm(ConsoleContext context, UUID chatID, Permission permission) {
        PermissionClientChain chain = new PermissionClientChain(context.client);
        context.io.chainManager.linkChain(chain);
        chain.removeDefault(chatID, permission);
        context.io.chainManager.removeChain(chain);
    }

    public static void addRolePerm(ConsoleContext context, UUID chatID, UUID roleID, Permission permission) {
        PermissionClientChain chain = new PermissionClientChain(context.client);
        context.io.chainManager.linkChain(chain);
        chain.addRole(chatID, roleID, permission);
        context.io.chainManager.removeChain(chain);
    }

    public static void removeRolePerm(ConsoleContext context, UUID chatID, UUID roleID, Permission permission) {
        PermissionClientChain chain = new PermissionClientChain(context.client);
        context.io.chainManager.linkChain(chain);
        chain.removeRole(chatID, roleID, permission);
        context.io.chainManager.removeChain(chain);
    }
}
