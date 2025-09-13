package net.result.main.commands.sandnode;

import net.result.main.commands.CommandInfo;
import net.result.main.commands.CommandRegistry;
import net.result.main.commands.ConsoleContext;
import net.result.sandnode.chain.sender.DEKClientChain;
import net.result.sandnode.dto.DEKResponseDTO;
import net.result.sandnode.encryption.SymmetricEncryptions;
import net.result.sandnode.encryption.interfaces.KeyStorage;
import net.result.sandnode.exception.error.KeyStorageNotFoundException;
import net.result.sandnode.key.GeneratedSource;
import net.result.sandnode.serverclient.SandnodeClient;
import net.result.sandnode.util.Member;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public class DEKCommands {

    public static void register(CommandRegistry registry) {
        registry.register(new CommandInfo("sendDEK", "Send a DEK to a receiver", "DEK", DEKCommands::sendDEK));
        registry.register(new CommandInfo("DEK", "List all DEKs", "DEK", DEKCommands::DEK));
    }

    public static void sendDEK(List<String> args, ConsoleContext context) {
        String receiver = args.get(0);
        final var client = context.client;
        final var source = new GeneratedSource();
        final var key = SymmetricEncryptions.AES.generate();

        KeyStorage encryptor;
        try {
            encryptor = client.node().agent().config.loadEncryptor(new Member(receiver, client.address));
        } catch (KeyStorageNotFoundException e) {
            DEKClientChain chain = new DEKClientChain(client);
            context.io.chainManager.linkChain(chain);
            encryptor = chain.getKeyOf(receiver).keyStorage();
            context.io.chainManager.removeChain(chain);
        }

        DEKClientChain chain = new DEKClientChain(client);
        context.io.chainManager.linkChain(chain);
        UUID uuid = chain.sendDEK(source, receiver, encryptor, key);
        context.io.chainManager.removeChain(chain);

        System.out.printf("DEK uuid: %s%n", uuid);
    }

    private static void DEK(List<String> ignoredArgs, ConsoleContext context) {
        SandnodeClient client = context.client;
        DEKClientChain chain = new DEKClientChain(client);
        context.io.chainManager.linkChain(chain);
        Collection<DEKResponseDTO> keys = chain.get();
        context.io.chainManager.removeChain(chain);

        keys.forEach(System.out::println);
    }
}
