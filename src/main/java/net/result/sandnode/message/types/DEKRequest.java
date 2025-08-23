package net.result.sandnode.message.types;

import net.result.sandnode.dto.DEKRequestDTO;
import net.result.sandnode.encryption.interfaces.KeyStorage;
import net.result.sandnode.message.MSGPackMessage;
import net.result.sandnode.message.RawMessage;
import net.result.sandnode.message.util.Headers;
import net.result.sandnode.message.util.MessageTypes;
import net.result.sandnode.util.DEKUtil;

public class DEKRequest extends MSGPackMessage<DEKRequestDTO> {
    public DEKRequest(DEKRequestDTO dto) {
        super(new Headers().setType(MessageTypes.DEK), dto);
    }

    public static DEKRequest send(String nickname, KeyStorage encryptor, KeyStorage keyStorage) {
        var dto = new DEKRequestDTO();
        dto.send = new DEKRequestDTO.Send(nickname, DEKUtil.getEncrypted(encryptor, keyStorage));
        return new DEKRequest(dto);
    }

    public static DEKRequest get() {
        DEKRequestDTO dto = new DEKRequestDTO();
        dto.get = true;
        return new DEKRequest(dto);
    }

    public static DEKRequest getPersonalKeyOf(String nickname) {
        DEKRequestDTO dto = new DEKRequestDTO();
        dto.getOf = nickname;
        return new DEKRequest(dto);
    }

    public DEKRequest(RawMessage raw) {
        super(raw.expect(MessageTypes.DEK), DEKRequestDTO.class);
    }
}
