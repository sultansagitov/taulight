package net.result.sandnode.config;

import net.result.sandnode.encryption.interfaces.SymmetricEncryption;
import net.result.sandnode.exception.FSException;
import net.result.sandnode.exception.crypto.KeyAlreadySaved;
import net.result.sandnode.util.Endpoint;
import net.result.sandnode.encryption.interfaces.AsymmetricKeyStorage;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public interface ClientConfig {
    @NotNull SymmetricEncryption symmetricKeyEncryption();

    void saveKey(@NotNull Endpoint endpoint, @NotNull AsymmetricKeyStorage keyStorage)
            throws FSException, KeyAlreadySaved;

    Optional<AsymmetricKeyStorage> getPublicKey(@NotNull Endpoint endpoint);

    void saveMemberKey(String nickname, AsymmetricKeyStorage keyStorage) throws FSException;

    Optional<AsymmetricKeyStorage> loadMemberKey(String nickname);
}
