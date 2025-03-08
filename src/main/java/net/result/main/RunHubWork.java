package net.result.main;

import net.result.main.config.JWTPropertiesConfig;
import net.result.sandnode.config.ServerConfig;
import net.result.sandnode.exception.*;
import net.result.sandnode.exception.crypto.EncryptionTypeException;
import net.result.sandnode.exception.crypto.NoSuchEncryptionException;
import net.result.sandnode.link.Links;
import net.result.sandnode.security.PasswordHashers;
import net.result.taulight.db.mariadb.TauMariaDBDatabase;
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
import org.mariadb.jdbc.MariaDbDataSource;

import java.net.URI;

public class RunHubWork implements IWork {

    private static final Logger LOGGER = LogManager.getLogger(RunHubWork.class);

    @Override
    public void run() throws SandnodeException {
        ServerConfig serverConfig = getServerConfig();

        AsymmetricEncryption mainEncryption = serverConfig.mainEncryption();

        KeyStorageRegistry keyStorageRegistry = serverConfig.readKey(mainEncryption);

        TauHub hub = new TauHub(keyStorageRegistry);
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

        MariaDbDataSource dataSource = new MariaDbDataSource();
        try {
            dataSource.setUrl("jdbc:mariadb://localhost:3306/taulight");
            dataSource.setUser("root");
            dataSource.setPassword("12345678");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        try {
            serverConfig.setDatabase(new TauMariaDBDatabase(dataSource, PasswordHashers.BCRYPT));
        } catch (DatabaseException e) {
            LOGGER.error(e);
            throw new RuntimeException(e);
        }
        serverConfig.setTokenizer(new JWTTokenizer(new JWTPropertiesConfig()));
        return serverConfig;
    }
}
