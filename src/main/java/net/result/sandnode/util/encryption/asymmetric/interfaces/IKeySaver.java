package net.result.sandnode.util.encryption.asymmetric.interfaces;

import net.result.sandnode.config.HubConfig;
import net.result.sandnode.exceptions.ReadingKeyException;
import net.result.sandnode.util.encryption.interfaces.IKeyStorage;

import java.io.IOException;

public interface IKeySaver {

    void saveKeys(HubConfig hubConfig, IKeyStorage keyStore) throws IOException, ReadingKeyException;

}
