package net.result.sandnode.util.encryption.interfaces;

import net.result.sandnode.config.IHubConfig;
import net.result.sandnode.exceptions.CreatingKeyException;

import java.io.IOException;

public interface IAsymmetricKeyReader {

    IKeyStorage readKeys(IHubConfig hubConfig) throws IOException, CreatingKeyException;

}
