package net.result.sandnode.config;

import net.result.sandnode.encryption.interfaces.AsymmetricKeyStorage;
import net.result.sandnode.encryption.interfaces.KeyStorage;
import net.result.sandnode.exception.StorageException;
import net.result.sandnode.exception.crypto.KeyAlreadySaved;
import net.result.sandnode.exception.error.KeyStorageNotFoundException;
import net.result.sandnode.util.Address;
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
    void saveServerKey(@NotNull Address address, @NotNull AsymmetricKeyStorage keyStorage)
            throws KeyAlreadySaved, StorageException;

    /**
     * Retrieves the server's public key.
     *
     * @param address server address or identifier
     * @return the server's public key storage
     * @throws KeyStorageNotFoundException if no key is found for the server
     */
    AsymmetricKeyStorage loadServerKey(@NotNull Address address) throws KeyStorageNotFoundException;

    /**
     * Saves this agent's personal asymmetric key pair.
     *
     * @param address    the agent's address or identifier
     * @param nickname   nickname of member
     * @param keyStorage personal key storage (private + public keys)
     * @throws StorageException if saving fails
     */
    void savePersonalKey(Address address, String nickname, KeyStorage keyStorage) throws StorageException;

    /**
     * Saves an encryptor's public key for a given agent, identified by a nickname.
     *
     * @param address    the target agent's address or identifier
     * @param nickname   nickname of member
     * @param keyStorage public key storage of the encryptor
     * @throws StorageException if saving fails
     */
    void saveEncryptor(Address address, String nickname, KeyStorage keyStorage) throws StorageException;

    /**
     * Saves a data encryption key (DEK) for encrypting actual data.
     *
     * @param address    the owner or context of the DEK
     * @param nickname   nickname of member
     * @param keyID      unique ID of the DEK
     * @param keyStorage symmetric key storage of the DEK
     * @throws StorageException if saving fails
     */
    void saveDEK(Address address, String nickname, UUID keyID, KeyStorage keyStorage) throws StorageException;

    /**
     * Loads this agent's personal key pair by its ID.
     *
     * @param address  agent address or identifier
     * @param nickname nickname of member
     * @throws KeyStorageNotFoundException if key not found
     */
    KeyStorage loadPersonalKey(Address address, String nickname) throws KeyStorageNotFoundException;

    /**
     * Loads an encryptor key by nickname.
     *
     * @param address  target agent address or identifier
     * @param nickname nickname of member
     * @return KeyEntry containing the encryptor key
     * @throws KeyStorageNotFoundException if key not found
     */
    KeyStorage loadEncryptor(Address address, String nickname) throws KeyStorageNotFoundException;

    /**
     * Loads a DEK by nickname.
     *
     * @param address  owner or context address
     * @param nickname nickname of member
     * @return KeyEntry containing the DEK
     * @throws KeyStorageNotFoundException if DEK not found
     */
    KeyEntry loadDEK(Address address, String nickname) throws KeyStorageNotFoundException;

    /**
     * Loads a DEK by its unique ID.
     *
     * @param address owner or context address
     * @param keyID   unique ID of the DEK
     * @return DEK key storage
     * @throws KeyStorageNotFoundException if DEK not found
     */
    KeyStorage loadDEK(Address address, UUID keyID) throws KeyStorageNotFoundException;
}