package net.result.main;

import net.result.sandnode.exceptions.CreatingKeyException;
import net.result.sandnode.exceptions.NoSuchReqHandler;
import net.result.sandnode.exceptions.ReadingKeyException;
import net.result.sandnode.exceptions.encryption.CannotUseEncryption;
import net.result.sandnode.exceptions.encryption.DecryptionException;
import net.result.sandnode.exceptions.encryption.EncryptionException;
import net.result.sandnode.exceptions.encryption.NoSuchEncryptionException;

import java.io.IOException;

public interface IWork {
    void run() throws IOException, ReadingKeyException, NoSuchEncryptionException, DecryptionException,
            EncryptionException, NoSuchReqHandler, CreatingKeyException, CannotUseEncryption;
}
