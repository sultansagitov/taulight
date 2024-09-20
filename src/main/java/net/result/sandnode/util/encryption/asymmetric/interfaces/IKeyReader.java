package net.result.sandnode.util.encryption.asymmetric.interfaces;

import net.result.sandnode.exceptions.CreatingKeyException;
import net.result.sandnode.util.encryption.interfaces.IKeyStorage;

import java.io.IOException;

public interface IKeyReader {

    IKeyStorage readKeys() throws IOException, CreatingKeyException;

}
