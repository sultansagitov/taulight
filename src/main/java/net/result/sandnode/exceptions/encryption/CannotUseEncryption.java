package net.result.sandnode.exceptions.encryption;

import net.result.sandnode.exceptions.SandnodeException;
import net.result.sandnode.util.encryption.interfaces.IEncryption;

public class CannotUseEncryption extends SandnodeException {

    public CannotUseEncryption(IEncryption encryption) {
        super(encryption.name());
    }

    public CannotUseEncryption(String encryption) {
        super(encryption);
    }
}
