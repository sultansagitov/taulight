package net.result.taulight;

import net.result.sandnode.exception.*;
import net.result.sandnode.util.IOController;
import net.result.taulight.chain.TauOnlineClientChain;

import java.util.Collection;

public class TauAgentProtocol {
    public static Collection<String> getOnline(IOController io)
            throws ExpectedMessageException, InterruptedException, DeserializationException {
        TauOnlineClientChain tauOnlineChain = new TauOnlineClientChain(io);
        io.chainManager.linkChain(tauOnlineChain);
        tauOnlineChain.sync();
        io.chainManager.removeChain(tauOnlineChain);
        return tauOnlineChain.members;
    }
}
