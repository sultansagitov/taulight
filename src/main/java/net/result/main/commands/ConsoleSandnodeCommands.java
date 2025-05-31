package net.result.main.commands;

import net.result.sandnode.chain.IChain;
import net.result.sandnode.chain.sender.*;
import net.result.sandnode.dto.FileDTO;
import net.result.sandnode.dto.LoginHistoryDTO;
import net.result.sandnode.encryption.SymmetricEncryptions;
import net.result.sandnode.encryption.interfaces.KeyStorage;
import net.result.sandnode.exception.error.KeyStorageNotFoundException;
import net.result.sandnode.hubagent.Agent;
import net.result.sandnode.hubagent.ClientProtocol;
import net.result.sandnode.dto.DEKDTO;

import java.time.format.DateTimeFormatter;
import java.util.*;

@SuppressWarnings("SameReturnValue")
public class ConsoleSandnodeCommands {
    public static void register(Map<String, LoopCondition> commands) {
        commands.put("keyID", ConsoleSandnodeCommands::keyID);
        commands.put("chains", ConsoleSandnodeCommands::chains);
        commands.put("groups", ConsoleSandnodeCommands::groups);
        commands.put("addGroup", ConsoleSandnodeCommands::addGroup);
        commands.put("rmGroup", ConsoleSandnodeCommands::rmGroup);
        commands.put("whoami", ConsoleSandnodeCommands::whoami);
        commands.put("name", ConsoleSandnodeCommands::name);
        commands.put("getAvatar", ConsoleSandnodeCommands::getAvatar);
        commands.put("setAvatar", ConsoleSandnodeCommands::setAvatar);
        commands.put("logout", ConsoleSandnodeCommands::logout);
        commands.put("loginHistory", ConsoleSandnodeCommands::loginHistory);
        commands.put("sendDEK", ConsoleSandnodeCommands::sendDEK);
        commands.put("DEK", ConsoleSandnodeCommands::DEK);
    }

    private static void keyID(List<String> ignored, ConsoleContext context) {
        System.out.println(context.keyID);
    }

    private static void chains(List<String> ignored, ConsoleContext context) {
        Collection<IChain> chains = context.io.chainManager.getAllChains();
        Map<String, IChain> map = context.io.chainManager.getChainsMap();

        System.out.printf("All client chains: %s%n", chains);
        System.out.printf("All named client chains: %s%n", map);
    }

    private static void groups(List<String> ignored, ConsoleContext context) throws Exception {
        Collection<String> groups = ClientProtocol.getGroups(context.client);
        System.out.printf("Your groups: %s%n", groups);
    }

    private static void addGroup(List<String> groups, ConsoleContext context) throws Exception {
        Collection<String> groupsAfterAdding = ClientProtocol.addToGroups(context.client, groups);
        System.out.printf("Your groups now (after adding): %s%n", groupsAfterAdding);
    }

    private static void rmGroup(List<String> groups, ConsoleContext context) throws Exception {
        Collection<String> groupsAfterRemoving = ClientProtocol.removeFromGroups(context.client, groups);
        System.out.printf("Your groups now (after removing): %s%n", groupsAfterRemoving);
    }

    private static void whoami(List<String> ignored, ConsoleContext context) throws Exception {
        WhoAmIClientChain chain = new WhoAmIClientChain(context.client);
        context.io.chainManager.linkChain(chain);
        String userID = chain.getNickname();

        context.io.chainManager.removeChain(chain);
        System.out.println(userID);
    }

    private static void name(List<String> ignored, ConsoleContext context) throws Exception {
        NameClientChain chain = new NameClientChain(context.client);
        context.io.chainManager.linkChain(chain);
        System.out.printf("Hub name: %s%n", chain.getName());
        context.io.chainManager.removeChain(chain);
    }

    private static void getAvatar(List<String> ignored, ConsoleContext context) throws Exception {
        var chain = new WhoAmIClientChain(context.client);
        context.io.chainManager.linkChain(chain);
        FileDTO avatar = chain.getAvatar();
        context.io.chainManager.removeChain(chain);

        if (avatar == null) {
            System.out.println("You have no avatar");
        } else {
            String mimeType = avatar.contentType();
            String base64 = Base64.getEncoder().encodeToString(avatar.body());
            System.out.printf("data:%s;base64,%s%n", mimeType, base64);
        }
    }

    private static void setAvatar(List<String> args, ConsoleContext context) throws Exception {
        Optional<String> path = args.stream().findFirst();
        if (path.isEmpty()) {
            System.out.println("Usage: setAvatar <path>");
            return;
        }

        var chain = new WhoAmIClientChain(context.client);
        context.io.chainManager.linkChain(chain);
        chain.setAvatar(path.get());
        context.io.chainManager.removeChain(chain);
    }

    private static void logout(List<String> ignored, ConsoleContext context) throws Exception {
        var chain = new LogoutClientChain(context.client);
        context.io.chainManager.linkChain(chain);
        chain.logout();
        context.io.chainManager.removeChain(chain);
    }

    private static void loginHistory(List<String> ignored, ConsoleContext context) throws Exception {
        var chain = new LoginClientChain(context.client);
        context.io.chainManager.linkChain(chain);
        List<LoginHistoryDTO> h = chain.getHistory();
        context.io.chainManager.removeChain(chain);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss z");

        for (LoginHistoryDTO dto : h) {
            System.out.printf("Time: %s, IP: %s, Device: %s%n", dto.time.format(formatter), dto.ip, dto.device);
        }
    }

    public static void sendDEK(List<String> args, ConsoleContext context) throws Exception {
        String nickname = args.get(0);

        DEKClientChain chain = new DEKClientChain(context.client);
        context.io.chainManager.linkChain(chain);

        UUID encryptorID;
        try {
            encryptorID = ((Agent) context.client.node).config.loadEncryptor(nickname).id();
        } catch (KeyStorageNotFoundException ignored) {
            encryptorID = chain.getKeyOf(nickname).keyID();
        }

        KeyStorage key = SymmetricEncryptions.AES.generate();
        UUID uuid = chain.sendDEK(nickname, encryptorID, key);
        context.io.chainManager.removeChain(chain);

        ((Agent) context.client.node).config.saveDEK(nickname, uuid, key);
    }

    private static void DEK(List<String> ignoredArgs, ConsoleContext context) throws Exception {
        DEKClientChain chain = new DEKClientChain(context.client);
        context.io.chainManager.linkChain(chain);
        Collection<DEKDTO> keys = chain.get();
        context.io.chainManager.removeChain(chain);

        for (DEKDTO key : keys) {
            KeyStorage decrypted = key.decrypt(((Agent) context.client.node).config.loadPersonalKey(key.encryptorID));

            ((Agent) context.client.node).config.saveDEK(key.senderNickname, key.id, decrypted);

            System.out.println(decrypted);
        }
    }
}
