package net.result.taulight.chain.sender;

import net.result.sandnode.chain.ClientChain;
import net.result.sandnode.exception.ProtocolException;
import net.result.sandnode.exception.error.SandnodeErrorException;
import net.result.sandnode.message.util.MessageTypes;
import net.result.sandnode.serverclient.SandnodeClient;
import net.result.taulight.dto.CodeDTO;
import net.result.taulight.dto.CodeRequestDTO;
import net.result.taulight.message.types.CodeRequest;
import net.result.taulight.message.types.CodeResponse;

public class CodeClientChain extends ClientChain {
    public CodeClientChain(SandnodeClient client) {
        super(client);
    }

    public CodeDTO check(String code) throws ProtocolException, InterruptedException, SandnodeErrorException {
        var raw = sendAndReceive(new CodeRequest(CodeRequestDTO.check(code)));
        return new CodeResponse(raw).check().code;
    }

    public void use(String code) throws ProtocolException, SandnodeErrorException, InterruptedException {
        sendAndReceive(new CodeRequest(CodeRequestDTO.use(code))).expect(MessageTypes.HAPPY);
    }
}
