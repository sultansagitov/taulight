package net.result.sandnode.config;

import net.result.sandnode.encryption.interfaces.AsymmetricKeyStorage;
import net.result.sandnode.encryption.interfaces.KeyStorage;
import net.result.sandnode.exception.StorageException;
import net.result.sandnode.exception.crypto.KeyAlreadySaved;
import net.result.sandnode.exception.error.KeyStorageNotFoundException;
import net.result.sandnode.util.Address;
import net.result.sandnode.util.Member;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * AgentConfig defines the configuration interface for an agent.
 *
 * <p><b>Terminology:</b></p>
 * <ul>
 *   <li><b>Server Public Key:</b> Public key of the server; private key remains securely on the server.</li>
 *   <li><b>Personal Key:</b> Full asymmetric key pair (public/private) of the agent.</li>
 *   <li><b>Encryptor:</b> Public keys of other agents used to encrypt data keys.</li>
 *   <li><b>DEK (Data Encryption Key):</b> Symmetric keys used for encrypting data.</li>
 * </ul>
 */
public interface AgentConfig {

    /**
     * Saves the server's public key.
     *
     * @param address    server address or identifier
     * @param keyStorage server's public key storage
     * @throws KeyAlreadySaved  if the server public key is already saved
     * @throws StorageException if an error occurs during saving
     */
    void saveServerKey(@NotNull Address address, @NotNull AsymmetricKeyStorage keyStorage);

    /**
     * Retrieves the server's public key.
     *
     * @param address server address or identifier
     * @return the server's public key storage
     * @throws KeyStorageNotFoundException if no key is found for the server
     */
    AsymmetricKeyStorage loadServerKey(@NotNull Address address);

    /**
     * Saves this agent's personal asymmetric key pair.
     *
     * @param member     the agent (address + nickname)
     * @param keyStorage personal key storage (private + public keys)
     * @throws StorageException if saving fails
     */
    void savePersonalKey(Member member, KeyStorage keyStorage);

    /**
     * Saves a public key of another agent (encryptor).
     *
     * @param member     target agent (address + nickname)
     * @param keyStorage public key storage of the encryptor
     * @throws StorageException if saving fails
     */
    void saveEncryptor(Member member, KeyStorage keyStorage);

    /**
     * Saves a data encryption key (DEK).
     *
     * @param member     owner of the DEK (address + nickname)
     * @param keyID      unique ID of the DEK
     * @param keyStorage symmetric key storage of the DEK
     * @throws StorageException if saving fails
     */
    void saveDEK(Member member, UUID keyID, KeyStorage keyStorage);

    /**
     * Loads this agent's personal key pair.
     *
     * @param member (address + nickname)
     * @return personal key storage
     * @throws KeyStorageNotFoundException if key not found
     */
    KeyStorage loadPersonalKey(Member member);

    /**
     * Loads a stored encryptor key.
     *
     * @param member target agent (address + nickname)
     * @return KeyEntry containing the encryptor key
     * @throws KeyStorageNotFoundException if key not found
     */
    KeyStorage loadEncryptor(Member member);

    /**
     * Loads a DEK by owner (identified by member).
     *
     * @param member owner of the DEK (address + nickname)
     * @return KeyEntry containing the DEK
     * @throws KeyStorageNotFoundException if DEK not found
     */
    KeyEntry loadDEK(Member member);

    /**
     * Loads a DEK by its unique ID.
     *
     * @param address owner or context address
     * @param keyID   unique ID of the DEK
     * @return DEK key storage
     * @throws KeyStorageNotFoundException if DEK not found
     */
    KeyStorage loadDEK(Address address, UUID keyID);
}