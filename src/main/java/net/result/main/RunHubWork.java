package net.result.main;

import net.result.main.config.HubPropertiesConfig;
import net.result.main.config.JWTPropertiesConfig;
import net.result.sandnode.config.HubConfig;
import net.result.sandnode.config.ServerConfig;
import net.result.sandnode.group.GroupManager;
import net.result.sandnode.security.Tokenizer;
import net.result.sandnode.util.JPAUtil;
import net.result.sandnode.exception.ConfigurationException;
import net.result.sandnode.exception.FSException;
import net.result.sandnode.exception.SandnodeException;
import net.result.sandnode.exception.SocketAcceptException;
import net.result.sandnode.exception.crypto.EncryptionTypeException;
import net.result.sandnode.exception.crypto.NoSuchEncryptionException;
import net.result.sandnode.link.SandnodeLinkRecord;
import net.result.sandnode.util.Container;
import net.result.taulight.db.ReactionPackageEntity;
import net.result.taulight.db.ReactionPackageRepository;
import net.result.taulight.db.ReactionTypeRepository;
import net.result.taulight.group.HashSetTauGroupManager;
import net.result.sandnode.security.JWTTokenizer;
import net.result.taulight.group.TauGroupManager;
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
        Container container = new Container();
        container.get(JPAUtil.class);

        ServerConfig serverConfig = getServerConfig(container);

        AsymmetricEncryption mainEncryption = serverConfig.mainEncryption();

        KeyStorageRegistry keyStorageRegistry = serverConfig.readKey(mainEncryption);

        HubConfig hubConfig = new HubPropertiesConfig();
        container.addInstance(HubConfig.class, hubConfig);

        TauHub hub = new TauHub(keyStorageRegistry, hubConfig);
        SandnodeServer server = new SandnodeServer(hub, serverConfig);

        createReactions(server);

        server.start();

        URI link = SandnodeLinkRecord.fromServer(server).getURI();
        System.out.println("Link for server:");
        System.out.println();
        System.out.println(link);
        System.out.println();

        try {
            server.acceptSessions();
        } catch (SocketAcceptException ignored) {
        }
    }

    private static void createReactions(SandnodeServer server) {
        ReactionPackageRepository reactionPackageRepo = server.container.get(ReactionPackageRepository.class);
        ReactionTypeRepository reactionTypeRepo = server.container.get(ReactionTypeRepository.class);

        try {
            if (reactionPackageRepo.find("taulight").isEmpty()) {
                ReactionPackageEntity rp = reactionPackageRepo.create("taulight", "Main package of taulight");
                reactionTypeRepo.create(rp, List.of("fire", "like", "laugh", "wow", "sad", "angry"));
            }
        } catch (Exception e) {
            LOGGER.error(e);
            throw new RuntimeException(e);
        }
    }

    private static @NotNull ServerPropertiesConfig getServerConfig(Container container)
            throws ConfigurationException, FSException, NoSuchEncryptionException, EncryptionTypeException {
        ServerPropertiesConfig serverConfig = new ServerPropertiesConfig(container);
        container.addInstance(ServerConfig.class, serverConfig);

        HashSetTauGroupManager groupManager = new HashSetTauGroupManager();
        container.addInstance(GroupManager.class, groupManager);
        container.addInstance(TauGroupManager.class, groupManager);
        container.addInstance(Tokenizer.class, new JWTTokenizer(container, new JWTPropertiesConfig()));

        return serverConfig;
    }
}