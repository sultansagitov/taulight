package net.result.sandnode.util.encryption.asymmetric.interfaces;

import net.result.sandnode.config.HubConfig;
import net.result.sandnode.exceptions.CreatingKeyException;
import net.result.sandnode.util.encryption.core.interfaces.IKeyStorage;

import java.io.IOException;

public interface IAsymmetricKeyReader {

    IKeyStorage readKeys(HubConfig hubConfig) throws IOException, CreatingKeyException;

}
