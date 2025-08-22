package net.result.taulight.chain.sender;

import net.result.sandnode.chain.ClientChain;
import net.result.sandnode.message.util.MessageTypes;
import net.result.sandnode.serverclient.SandnodeClient;
import net.result.taulight.dto.CodeDTO;
import net.result.taulight.dto.CodeRequestDTO;
import net.result.taulight.message.CodeListMessage;
import net.result.taulight.message.TauMessageTypes;
import net.result.taulight.message.types.CodeRequest;
import net.result.taulight.message.types.CodeResponse;

import java.util.Collection;
import java.util.UUID;

public class CodeClientChain extends ClientChain {
    public CodeClientChain(SandnodeClient client) {
        super(client);
    }

    public synchronized CodeDTO check(String code) {
        var raw = sendAndReceive(new CodeRequest(CodeRequestDTO.check(code)));
        return new CodeResponse(raw).check().code;
    }

    public synchronized void use(String code) {
        sendAndReceive(new CodeRequest(CodeRequestDTO.use(code))).expect(MessageTypes.HAPPY);
    }

    public synchronized Collection<CodeDTO> getGroupCodes(UUID chatID) {
        var raw = sendAndReceive(new CodeRequest(CodeRequestDTO.groupCodes(chatID)));
        raw.expect(TauMessageTypes.CODE);
        return new CodeListMessage(raw).codes();
    }

    public synchronized Collection<CodeDTO> getMyCodes() {
        var raw = sendAndReceive(new CodeRequest(CodeRequestDTO.myCodes()));
        raw.expect(TauMessageTypes.CODE);
        return new CodeListMessage(raw).codes();
    }

}
