package net.result.sandnode.chain.sender;

import net.result.sandnode.chain.ClientChain;
import net.result.sandnode.dto.PublicKeyDTO;
import net.result.sandnode.dto.RegisterRequestDTO;
import net.result.sandnode.dto.RegistrationResponseDTO;
import net.result.sandnode.encryption.AsymmetricEncryptions;
import net.result.sandnode.encryption.interfaces.AsymmetricKeyStorage;
import net.result.sandnode.exception.error.BusyNicknameException;
import net.result.sandnode.exception.error.InvalidNicknamePassword;
import net.result.sandnode.key.GeneratedSource;
import net.result.sandnode.message.RawMessage;
import net.result.sandnode.message.types.RegistrationRequest;
import net.result.sandnode.message.types.RegistrationResponse;
import net.result.sandnode.serverclient.SandnodeClient;
import net.result.sandnode.util.Member;
import org.jetbrains.annotations.NotNull;

public class RegistrationClientChain extends ClientChain {
    public RegistrationClientChain(SandnodeClient client) {
        super(client);
    }

    public synchronized RegistrationResponseDTO register(
            @NotNull String nickname,
            @NotNull String password,
            @NotNull String device
    ) {
        return register(nickname, password, device, AsymmetricEncryptions.ECIES.generate());
    }

    public synchronized RegistrationResponseDTO register(
            @NotNull String nickname,
            @NotNull String password,
            @NotNull String device,
            @NotNull AsymmetricKeyStorage keyStorage
    ) {
        var pubDTO = new PublicKeyDTO(keyStorage);
        var regDTO = new RegisterRequestDTO(nickname, password, device, pubDTO);

        RegistrationRequest request = new RegistrationRequest(regDTO);
        send(request);
        RawMessage response = receiveWithSpecifics(BusyNicknameException.class, InvalidNicknamePassword.class);

        client.node().agent().config.savePersonalKey(new GeneratedSource(), new Member(nickname, client.address), keyStorage);

        client.nickname = nickname;

        return new RegistrationResponse(response).dto();
    }
}
