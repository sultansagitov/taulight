package net.result.main;

import net.result.sandnode.encryption.interfaces.IAsymmetricConvertor;
import net.result.sandnode.encryption.interfaces.AsymmetricKeyStorage;
import net.result.sandnode.exception.*;
import net.result.sandnode.link.Links;
import net.result.sandnode.util.FileUtil;
import net.result.sandnode.db.InMemoryDatabase;
import net.result.sandnode.group.HashSetGroupManager;
import net.result.sandnode.tokens.JWTConfig;
import net.result.sandnode.tokens.JWTTokenizer;
import net.result.taulight.TauChatManager;
import net.result.taulight.TauHub;
import net.result.main.config.ServerPropertiesConfig;
import net.result.sandnode.serverclient.SandnodeServer;
import net.result.sandnode.encryption.GlobalKeyStorage;
import net.result.sandnode.encryption.interfaces.IAsymmetricEncryption;
import net.result.taulight.messenger.TauChat;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URI;
import java.nio.file.Path;

public class RunHubWork implements IWork {

    private static final Logger LOGGER = LogManager.getLogger(RunHubWork.class);

    @Override
    public void run() throws SandnodeException {
        ServerPropertiesConfig serverConfig = new ServerPropertiesConfig();
        serverConfig.setGroupManager(new HashSetGroupManager());
        serverConfig.setDatabase(new InMemoryDatabase());
        serverConfig.setTokenizer(new JWTTokenizer(new JWTConfig("YourSuperSecretKey")));

        IAsymmetricEncryption mainEncryption = serverConfig.mainEncryption();

        Path publicKeyPath = serverConfig.publicKeyPath();
        Path privateKeyPath = serverConfig.privateKeyPath();

        LOGGER.info("Reading public key in \"{}\"", publicKeyPath);
        IAsymmetricConvertor publicKeyConvertor = mainEncryption.publicKeyConvertor();
        String publicKeyString = FileUtil.readString(publicKeyPath);
        AsymmetricKeyStorage publicKeyStorage = publicKeyConvertor.toKeyStorage(publicKeyString);

        LOGGER.info("Reading private key in \"{}\"", privateKeyPath);
        IAsymmetricConvertor privateKeyConvertor = mainEncryption.privateKeyConvertor();
        String string = FileUtil.readString(privateKeyPath);
        AsymmetricKeyStorage privateKeyStorage = privateKeyConvertor.toKeyStorage(string);

        AsymmetricKeyStorage keyStorage = mainEncryption.merge(publicKeyStorage, privateKeyStorage);
        GlobalKeyStorage globalKeyStorage = new GlobalKeyStorage(keyStorage);

        TauChatManager chatManager = new TauChatManager();
        chatManager.addNew(new TauChat("first"));
        chatManager.addNew(new TauChat("second"));
        TauHub hub = new TauHub(globalKeyStorage, chatManager);
        SandnodeServer server = new SandnodeServer(hub, serverConfig);
        server.start();

        URI link = Links.getServerLink(server);
        System.out.println("Link for server:");
        System.out.println();
        System.out.println(link);
        System.out.println();

        try {
            server.acceptSessions();
        } catch (SocketAcceptException ignored) {
        }
    }

}
