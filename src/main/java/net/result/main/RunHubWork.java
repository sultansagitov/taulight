package net.result.main;

import net.result.sandnode.config.ServerConfig;
import net.result.sandnode.encryption.interfaces.AsymmetricConvertor;
import net.result.sandnode.encryption.interfaces.AsymmetricKeyStorage;
import net.result.sandnode.exception.*;
import net.result.sandnode.link.Links;
import net.result.sandnode.security.PasswordHashers;
import net.result.sandnode.util.FileUtil;
import net.result.taulight.mariadb.TauMariaDBDatabase;
import net.result.taulight.group.HashSetTauGroupManager;
import net.result.sandnode.tokens.JWTConfig;
import net.result.sandnode.tokens.JWTTokenizer;
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
import java.nio.file.Path;

public class RunHubWork implements IWork {

    private static final Logger LOGGER = LogManager.getLogger(RunHubWork.class);

    @Override
    public void run() throws SandnodeException {
        ServerConfig serverConfig = getServerConfig();

        AsymmetricEncryption mainEncryption = serverConfig.mainEncryption();

        Path publicKeyPath = serverConfig.publicKeyPath();
        Path privateKeyPath = serverConfig.privateKeyPath();

        LOGGER.info("Reading public key in \"{}\"", publicKeyPath);
        AsymmetricConvertor publicKeyConvertor = mainEncryption.publicKeyConvertor();
        String publicKeyString = FileUtil.readString(publicKeyPath);
        AsymmetricKeyStorage publicKeyStorage = publicKeyConvertor.toKeyStorage(publicKeyString);

        LOGGER.info("Reading private key in \"{}\"", privateKeyPath);
        AsymmetricConvertor privateKeyConvertor = mainEncryption.privateKeyConvertor();
        String string = FileUtil.readString(privateKeyPath);
        AsymmetricKeyStorage privateKeyStorage = privateKeyConvertor.toKeyStorage(string);

        AsymmetricKeyStorage keyStorage = mainEncryption.merge(publicKeyStorage, privateKeyStorage);
        KeyStorageRegistry keyStorageRegistry = new KeyStorageRegistry(keyStorage);

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
        serverConfig.setTokenizer(new JWTTokenizer(new JWTConfig("YourSuperSecretKey")));
        return serverConfig;
    }
}
