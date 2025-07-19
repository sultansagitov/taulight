package net.result.main.commands;

import net.result.sandnode.chain.ChainStorage;
import net.result.sandnode.chain.Chain;
import net.result.sandnode.chain.sender.*;
import net.result.sandnode.dto.FileDTO;
import net.result.sandnode.dto.LoginHistoryDTO;
import net.result.sandnode.encryption.SymmetricEncryptions;
import net.result.sandnode.encryption.interfaces.KeyStorage;
import net.result.sandnode.exception.error.KeyStorageNotFoundException;
import net.result.sandnode.hubagent.Agent;
import net.result.sandnode.hubagent.ClientProtocol;
import net.result.sandnode.dto.DEKDTO;
import org.jetbrains.annotations.Nullable;

import java.time.format.DateTimeFormatter;
import java.util.*;

@SuppressWarnings("SameReturnValue")
public class ConsoleSandnodeCommands {
    public static void register(Map<String, LoopCondition> commands) {
        commands.put("keyID", ConsoleSandnodeCommands::keyID);
        commands.put("chains", ConsoleSandnodeCommands::chains);
        commands.put("clusters", ConsoleSandnodeCommands::clusters);
        commands.put("addCluster", ConsoleSandnodeCommands::addCluster);
        commands.put("rmCluster", ConsoleSandnodeCommands::rmCluster);
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
        ChainStorage storage = context.io.chainManager.storage();
        Collection<Chain> chains = storage.getAll();
        Map<String, Chain> map = storage.getNamed();

        System.out.printf("All client chains: %s%n", chains);
        System.out.printf("All named client chains: %s%n", map);
    }

    private static void clusters(List<String> ignored, ConsoleContext context) throws Exception {
        Collection<String> clusters = ClientProtocol.getClusters(context.client);
        System.out.printf("Your clusters: %s%n", clusters);
    }

    private static void addCluster(List<String> clusters, ConsoleContext context) throws Exception {
        Collection<String> clustersAfterAdding = ClientProtocol.addToClusters(context.client, clusters);
        System.out.printf("Your clusters now (after adding): %s%n", clustersAfterAdding);
    }

    private static void rmCluster(List<String> clusters, ConsoleContext context) throws Exception {
        Collection<String> clustersAfterRemoving = ClientProtocol.removeFromClusters(context.client, clusters);
        System.out.printf("Your clusters now (after removing): %s%n", clustersAfterRemoving);
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

    private static void getAvatar(List<String> args, ConsoleContext context) throws Exception {
        Optional<String> nickname = args.stream().findFirst();

        var chain = new AvatarClientChain(context.client);
        context.io.chainManager.linkChain(chain);
        @Nullable FileDTO avatar = nickname.isPresent()
                ? chain.getOf(nickname.get())
                : chain.getMy();
        context.io.chainManager.removeChain(chain);

        if (avatar == null) {
            System.out.println("Have no avatar");
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

        var chain = new AvatarClientChain(context.client);
        context.io.chainManager.linkChain(chain);
        UUID uuid = chain.set(path.get());
        context.io.chainManager.removeChain(chain);

        System.out.printf("Image uuid: %s%n", uuid);
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

        Agent agent = context.client.node.agent();


        for (LoginHistoryDTO dto : h) {
            KeyStorage personalKey = agent.config.loadPersonalKey(context.client.address, dto.encryptorID);

            String ip = personalKey.decrypt(Base64.getDecoder().decode(dto.ip));
            String device = personalKey.decrypt(Base64.getDecoder().decode(dto.device));

            System.out.printf(
                    "Time: %s, IP: %s, Device: %s, Active: %s%n",
                    dto.time.format(formatter),
                    ip,
                    device,
                    dto.isOnline
            );
        }
    }

    public static void sendDEK(List<String> args, ConsoleContext context) throws Exception {
        String nickname = args.get(0);

        DEKClientChain chain = new DEKClientChain(context.client);
        context.io.chainManager.linkChain(chain);

        UUID encryptorID;
        Agent agent = context.client.node.agent();
        try {
            encryptorID = agent.config.loadEncryptor(context.client.address, nickname).id();
        } catch (KeyStorageNotFoundException ignored) {
            encryptorID = chain.getKeyOf(nickname).keyID();
        }

        KeyStorage key = SymmetricEncryptions.AES.generate();
        UUID uuid = chain.sendDEK(nickname, encryptorID, key);
        context.io.chainManager.removeChain(chain);

        agent.config.saveDEK(context.client.address, nickname, uuid, key);
    }

    private static void DEK(List<String> ignoredArgs, ConsoleContext context) throws Exception {
        DEKClientChain chain = new DEKClientChain(context.client);
        context.io.chainManager.linkChain(chain);
        Collection<DEKDTO> keys = chain.get();
        context.io.chainManager.removeChain(chain);

        for (DEKDTO key : keys) {
            Agent agent = context.client.node.agent();

            KeyStorage decrypted = key.decrypt(agent.config.loadPersonalKey(context.client.address, key.encryptorID));

            agent.config.saveDEK(context.client.address, key.senderNickname, key.id, decrypted);

            System.out.println(decrypted);
        }
    }
}
