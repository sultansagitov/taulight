package net.result.sandnode.util.encryption.asymmetric.interfaces;

import net.result.sandnode.exceptions.ReadingKeyException;
import net.result.sandnode.util.encryption.interfaces.IKeyStorage;

import java.io.IOException;

public interface IKeySaver {

    void saveKeys(IKeyStorage keyStore) throws IOException, ReadingKeyException;

}
