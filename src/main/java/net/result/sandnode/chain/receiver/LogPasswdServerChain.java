package net.result.sandnode.chain.receiver;

import net.result.sandnode.chain.ReceiverChain;
import net.result.sandnode.chain.ServerChain;
import net.result.sandnode.encryption.interfaces.AsymmetricEncryption;
import net.result.sandnode.encryption.interfaces.AsymmetricKeyStorage;
import net.result.sandnode.entity.KeyStorageEntity;
import net.result.sandnode.entity.LoginEntity;
import net.result.sandnode.entity.MemberEntity;
import net.result.sandnode.exception.error.UnauthorizedException;
import net.result.sandnode.hubagent.Hub;
import net.result.sandnode.message.RawMessage;
import net.result.sandnode.message.types.LogPasswdRequest;
import net.result.sandnode.message.types.LogPasswdResponse;
import net.result.sandnode.repository.LoginRepository;
import net.result.sandnode.repository.MemberRepository;
import net.result.sandnode.security.PasswordHasher;
import net.result.sandnode.security.Tokenizer;

import java.util.Base64;

public abstract class LogPasswdServerChain extends ServerChain implements ReceiverChain {

    @Override
    public LogPasswdResponse handle(RawMessage raw) {
        LogPasswdRequest request = new LogPasswdRequest(raw);

        Tokenizer tokenizer = session.server.container.get(Tokenizer.class);
        LoginRepository loginRepo = session.server.container.get(LoginRepository.class);
        MemberRepository memberRepo = session.server.container.get(MemberRepository.class);
        Hub hub = session.node().hub();
        PasswordHasher hasher = hub.config.hasher();

        MemberEntity member = memberRepo
                .findByNickname(request.dto().nickname)
                .orElseThrow(UnauthorizedException::new);

        boolean verified = hasher.verify(request.dto().password, member.getPasswordHash());
        if (!verified) {
            throw new UnauthorizedException();
        }

        String ip = session.io().socket.getInetAddress().getHostAddress();

        KeyStorageEntity keyEntity = member.getPublicKey();
        AsymmetricEncryption encryption = keyEntity.encryption().asymmetric();
        AsymmetricKeyStorage keyStorage = encryption.publicKeyConvertor().toKeyStorage(keyEntity.encodedKey());

        String encryptedIP = Base64.getEncoder().encodeToString(keyStorage.encrypt(ip));
        String encryptedDevice = Base64.getEncoder().encodeToString(keyStorage.encrypt(request.dto().device));

        LoginEntity login = loginRepo.create(member, keyEntity, encryptedIP, encryptedDevice);

        session.member = member;
        session.login = login;

        onLogin();

        String token = tokenizer.tokenizeLogin(login);
        return new LogPasswdResponse(token);
    }

    protected abstract void onLogin();
}
