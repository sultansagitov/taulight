package net.result.main;

import net.result.main.config.HubPropertiesConfig;
import net.result.main.config.JWTPropertiesConfig;
import net.result.sandnode.config.HubConfig;
import net.result.sandnode.config.ServerConfig;
import net.result.sandnode.db.JPAUtil;
import net.result.sandnode.exception.ConfigurationException;
import net.result.sandnode.exception.FSException;
import net.result.sandnode.exception.SandnodeException;
import net.result.sandnode.exception.SocketAcceptException;
import net.result.sandnode.exception.crypto.EncryptionTypeException;
import net.result.sandnode.exception.crypto.NoSuchEncryptionException;
import net.result.sandnode.link.Links;
import net.result.taulight.db.TauDatabase;
import net.result.taulight.group.HashSetTauGroupManager;
import net.result.sandnode.security.JWTTokenizer;
import net.result.taulight.hubagent.TauHub;
import net.result.main.config.ServerPropertiesConfig;
import net.result.sandnode.serverclient.SandnodeServer;
import net.result.sandnode.encryption.KeyStorageRegistry;
import net.result.sandnode.encryption.interfaces.AsymmetricEncryption;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.net.URI;

public class RunHubWork implements IWork {

    private static final Logger LOGGER = LogManager.getLogger(RunHubWork.class);

    @Override
    public void run() throws SandnodeException {
        JPAUtil.buildEntityManagerFactory();

        ServerConfig serverConfig = getServerConfig();

        AsymmetricEncryption mainEncryption = serverConfig.mainEncryption();

        KeyStorageRegistry keyStorageRegistry = serverConfig.readKey(mainEncryption);

        HubConfig hubConfig = new HubPropertiesConfig();

        TauHub hub = new TauHub(keyStorageRegistry, hubConfig);
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

    private static @NotNull ServerPropertiesConfig getServerConfig()
            throws ConfigurationException, FSException, NoSuchEncryptionException, EncryptionTypeException {
        ServerPropertiesConfig serverConfig = new ServerPropertiesConfig();
        serverConfig.setGroupManager(new HashSetTauGroupManager());

        TauDatabase database = (TauDatabase) serverConfig.database();

        try {
            if (database.getReactionTypesByPackage("taulight").isEmpty()) {
                List<String> reactionNames = List.of("fire", "like", "laugh", "wow", "sad", "angry");

                for (String name : reactionNames) {
                    database.createReactionType(name, "taulight");
                }
            }
        } catch (Exception e) {
            LOGGER.error(e);
            throw new RuntimeException(e);
        }


        serverConfig.setTokenizer(new JWTTokenizer(new JWTPropertiesConfig()));
        return serverConfig;
    }
}