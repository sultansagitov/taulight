package net.result.main.hub;

import net.result.main.Work;
import net.result.main.config.HubPropertiesConfig;
import net.result.main.config.JWTPropertiesConfig;
import net.result.main.config.ServerPropertiesConfig;
import net.result.sandnode.cluster.ClusterManager;
import net.result.sandnode.config.HubConfig;
import net.result.sandnode.config.ServerConfig;
import net.result.sandnode.db.MemberCreationListener;
import net.result.sandnode.encryption.KeyStorageRegistry;
import net.result.sandnode.encryption.interfaces.AsymmetricEncryption;
import net.result.sandnode.exception.ConfigurationException;
import net.result.sandnode.exception.FSException;
import net.result.sandnode.exception.SandnodeException;
import net.result.sandnode.exception.SocketAcceptException;
import net.result.sandnode.exception.crypto.EncryptionTypeException;
import net.result.sandnode.exception.crypto.NoSuchEncryptionException;
import net.result.sandnode.security.JWTTokenizer;
import net.result.sandnode.security.Tokenizer;
import net.result.sandnode.serverclient.SandnodeServer;
import net.result.sandnode.util.Container;
import net.result.sandnode.util.SimpleContainer;
import net.result.sandnode.db.SimpleJPAUtil;
import net.result.taulight.cluster.HashSetTauClusterManager;
import net.result.taulight.cluster.TauClusterManager;
import net.result.taulight.entity.ReactionPackageEntity;
import net.result.taulight.repository.ReactionPackageRepository;
import net.result.taulight.repository.ReactionTypeRepository;
import net.result.taulight.db.TauMemberCreationListener;
import net.result.taulight.hubagent.TauHub;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class RunHubWork implements Work {
    private static final Logger LOGGER = LogManager.getLogger(RunHubWork.class);

    @Override
    public void run() throws SandnodeException {
        Container container = new SimpleContainer();
        container.get(SimpleJPAUtil.class);
        container.addInstanceItem(MemberCreationListener.class, new TauMemberCreationListener(container));

        ServerConfig serverConfig = getServerConfig(container);
        AsymmetricEncryption mainEncryption = serverConfig.mainEncryption();
        KeyStorageRegistry keyStorageRegistry = serverConfig.readKey(mainEncryption);

        HubConfig hubConfig = new HubPropertiesConfig();
        container.addInstance(HubConfig.class, hubConfig);

        TauHub hub = new TauHub(keyStorageRegistry, hubConfig);
        SandnodeServer server = new SandnodeServer(hub, serverConfig);

        createReactions(server);

        server.start();

        HubConsole console = new HubConsole(server);

        Thread thread = new Thread(() -> {
            try {
                server.acceptSessions();
            } catch (SocketAcceptException e) {
                if (!console.running) return;
                throw new RuntimeException(e);
            }
        }, "Socket-Accepting");
        thread.setDaemon(true);
        thread.start();

        console.start();
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

    private static @NotNull ServerConfig getServerConfig(Container container)
            throws ConfigurationException, FSException, NoSuchEncryptionException, EncryptionTypeException {
        ServerConfig serverConfig = new ServerPropertiesConfig(container);
        container.addInstance(ServerConfig.class, serverConfig);

        HashSetTauClusterManager clusterManager = new HashSetTauClusterManager();
        container.addInstance(ClusterManager.class, clusterManager);
        container.addInstance(TauClusterManager.class, clusterManager);
        container.addInstance(Tokenizer.class, new JWTTokenizer(container, new JWTPropertiesConfig()));

        return serverConfig;
    }
}
