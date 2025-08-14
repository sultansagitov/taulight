package net.result.sandnode.chain.receiver;

import net.result.sandnode.chain.ReceiverChain;
import net.result.sandnode.chain.ServerChain;
import net.result.sandnode.config.HubConfig;
import net.result.sandnode.dto.PublicKeyDTO;
import net.result.sandnode.dto.RegisterRequestDTO;
import net.result.sandnode.encryption.EncryptionManager;
import net.result.sandnode.encryption.interfaces.AsymmetricEncryption;
import net.result.sandnode.encryption.interfaces.AsymmetricKeyStorage;
import net.result.sandnode.entity.KeyStorageEntity;
import net.result.sandnode.entity.LoginEntity;
import net.result.sandnode.entity.MemberEntity;
import net.result.sandnode.exception.SandnodeException;
import net.result.sandnode.exception.error.InvalidNicknamePassword;
import net.result.sandnode.message.RawMessage;
import net.result.sandnode.message.types.RegistrationRequest;
import net.result.sandnode.message.types.RegistrationResponse;
import net.result.sandnode.repository.LoginRepository;
import net.result.sandnode.repository.MemberRepository;
import net.result.sandnode.security.PasswordHasher;
import net.result.sandnode.security.Tokenizer;
import net.result.sandnode.serverclient.Session;

import java.util.Base64;

public class RegistrationServerChain extends ServerChain implements ReceiverChain {
    public RegistrationServerChain(Session session) {
        super(session);
    }

    @Override
    public RegistrationResponse handle(RawMessage raw) throws SandnodeException {
        MemberRepository memberRepo = session.server.container.get(MemberRepository.class);
        LoginRepository loginRepo = session.server.container.get(LoginRepository.class);
        Tokenizer tokenizer = session.server.container.get(Tokenizer.class);
        HubConfig hubConfig = session.server.container.get(HubConfig.class);

        PasswordHasher hasher = hubConfig.hasher();

        RegistrationRequest regMsg = new RegistrationRequest(raw);

        RegisterRequestDTO dto = regMsg.dto();
        String nickname = dto.nickname;
        String password = dto.password;
        String device = dto.device;
        PublicKeyDTO pub = dto.keyStorage;

        AsymmetricEncryption encryption = EncryptionManager.find(pub.encryption).asymmetric();
        AsymmetricKeyStorage keyStorage = encryption.publicKeyConvertor().toKeyStorage(pub.encoded);

        if (nickname.isEmpty() || password.isEmpty()) {
            throw new InvalidNicknamePassword();
        }

        MemberEntity member = memberRepo.create(nickname, hasher.hash(password, 12), keyStorage);
        session.member = member;

        String ip = session.io().socket.getInetAddress().getHostAddress();

        String encryptedIP = Base64.getEncoder().encodeToString(keyStorage.encrypt(ip));
        String encryptedDevice = Base64.getEncoder().encodeToString(keyStorage.encrypt(device));

        KeyStorageEntity keyEntity = member.publicKey();
        LoginEntity login = loginRepo.create(member, keyEntity, encryptedIP, encryptedDevice);

        String token = tokenizer.tokenizeLogin(login);
        return new RegistrationResponse(token);
    }
}
