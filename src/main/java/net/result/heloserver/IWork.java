package net.result.heloserver;

import net.result.sandnode.exceptions.ReadingKeyException;
import net.result.sandnode.exceptions.encryption.DecryptionException;
import net.result.sandnode.exceptions.encryption.EncryptionException;
import net.result.sandnode.exceptions.encryption.NoSuchEncryptionException;

import java.io.IOException;
import java.security.GeneralSecurityException;

public interface IWork {
    void run() throws GeneralSecurityException, IOException, ReadingKeyException, NoSuchEncryptionException, DecryptionException, EncryptionException;
}
