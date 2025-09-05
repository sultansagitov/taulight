package net.result.main.chain.receiver;

import net.result.sandnode.serverclient.SandnodeClient;
import net.result.taulight.chain.receiver.DownstreamClientChain;
import net.result.taulight.dto.ChatMessageInputDTO;
import net.result.taulight.dto.ChatMessageViewDTO;

public class ConsoleDownstreamClientChain extends DownstreamClientChain {
    public ConsoleDownstreamClientChain(SandnodeClient client) {
        super(client);
    }

    @Override
    public void onMessage(ChatMessageViewDTO serverMessage, String decrypted, boolean yourSession) {
        ChatMessageInputDTO message = serverMessage.message;
        String nickname = yourSession ? "You" : message.nickname;
        System.out.printf("%s > %s > %s%n", message.chatID, nickname, decrypted);
    }
}
