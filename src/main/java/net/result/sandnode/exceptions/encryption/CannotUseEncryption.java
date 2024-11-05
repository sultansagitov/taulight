package net.result.sandnode.exceptions.encryption;

import net.result.sandnode.exceptions.SandnodeException;

public class CannotUseEncryption extends SandnodeException {
    public CannotUseEncryption(String message) {
        super(message);
    }

    public CannotUseEncryption(String message, Exception e) {
        super(message, e);
    }
}
