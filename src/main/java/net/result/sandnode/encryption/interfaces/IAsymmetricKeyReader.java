package net.result.sandnode.encryption.interfaces;

import net.result.sandnode.config.IServerConfig;
import net.result.sandnode.exceptions.CreatingKeyException;
import net.result.sandnode.exceptions.FSException;

public interface IAsymmetricKeyReader {

    IKeyStorage readKeys(IServerConfig serverConfig) throws CreatingKeyException, FSException;

}
