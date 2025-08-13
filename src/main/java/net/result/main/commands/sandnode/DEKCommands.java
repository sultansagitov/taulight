package net.result.main.commands.sandnode;

import net.result.main.commands.CommandInfo;
import net.result.main.commands.CommandRegistry;
import net.result.main.commands.ConsoleContext;
import net.result.sandnode.chain.sender.DEKClientChain;
import net.result.sandnode.dto.DEKResponseDTO;
import net.result.sandnode.encryption.SymmetricEncryptions;
import net.result.sandnode.encryption.interfaces.KeyStorage;
import net.result.sandnode.exception.error.KeyStorageNotFoundException;
import net.result.sandnode.serverclient.SandnodeClient;

import java.util.*;

public class DEKCommands {

    public static void register(CommandRegistry registry) {
        registry.register(new CommandInfo("sendDEK", "Send a DEK to a receiver", "DEK", DEKCommands::sendDEK));
        registry.register(new CommandInfo("DEK", "List all DEKs", "DEK", DEKCommands::DEK));
    }

    public static void sendDEK(List<String> args, ConsoleContext context) throws Exception {
        String receiver = args.get(0);
        var client = context.client;
        var key = SymmetricEncryptions.AES.generate();

        KeyStorage encryptor;
        try {
            encryptor = client.node().agent().config.loadEncryptor(client.address, receiver);
        } catch (KeyStorageNotFoundException e) {
            DEKClientChain chain = new DEKClientChain(client);
            context.io.chainManager.linkChain(chain);
            encryptor = chain.getKeyOf(receiver).keyStorage();
            context.io.chainManager.removeChain(chain);
        }

        DEKClientChain chain = new DEKClientChain(client);
        context.io.chainManager.linkChain(chain);
        UUID uuid = chain.sendDEK(receiver, encryptor, key);
        context.io.chainManager.removeChain(chain);

        System.out.printf("DEK uuid: %s%n", uuid);
    }

    private static void DEK(List<String> ignoredArgs, ConsoleContext context) throws Exception {
        SandnodeClient client = context.client;
        DEKClientChain chain = new DEKClientChain(client);
        context.io.chainManager.linkChain(chain);
        Collection<DEKResponseDTO> keys = chain.get();
        context.io.chainManager.removeChain(chain);

        keys.forEach(System.out::println);
    }
}
