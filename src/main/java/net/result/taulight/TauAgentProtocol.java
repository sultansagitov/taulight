package net.result.taulight;

import net.result.sandnode.exceptions.*;
import net.result.sandnode.util.IOControl;
import net.result.taulight.chain.TauOnlineClientChain;

import java.util.Set;

public class TauAgentProtocol {
    public static Set<String> getOnline(IOControl io)
            throws ExpectedMessageException, InterruptedException, DeserializationException {
        TauOnlineClientChain tauOnlineChain = new TauOnlineClientChain(io);
        io.chainManager.linkChain(tauOnlineChain);
        tauOnlineChain.sync();
        io.chainManager.removeChain(tauOnlineChain);
        return tauOnlineChain.members;
    }
}
