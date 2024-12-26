package net.result.taulight;

import net.result.sandnode.exceptions.*;
import net.result.sandnode.util.IOControl;
import net.result.taulight.chain.TauOnlineChain;

import java.util.Set;

public class TauAgentProtocol {
    public static Set<String> getOnline(IOControl io) throws EncryptionTypeException, MemberNotFound,
            NoSuchEncryptionException, ExpectedMessageException, CreatingKeyException, KeyNotCreatedException,
            KeyStorageNotFoundException, InterruptedException, DataNotEncryptedException {
        TauOnlineChain chain = new TauOnlineChain(io);
        io.chainManager.addChain(chain);
        chain.sync();
        return chain.members;
    }
}
