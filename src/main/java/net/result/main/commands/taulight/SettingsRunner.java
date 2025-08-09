package net.result.main.commands.taulight;

import net.result.main.commands.ConsoleContext;
import net.result.taulight.chain.sender.TauMemberSettingsClientChain;
import net.result.taulight.dto.TauMemberSettingsResponseDTO;

public class SettingsRunner {
    public static TauMemberSettingsResponseDTO get(ConsoleContext context) throws Exception {
        TauMemberSettingsClientChain chain = new TauMemberSettingsClientChain(context.client);
        context.io.chainManager.linkChain(chain);
        TauMemberSettingsResponseDTO dto = chain.get();
        context.io.chainManager.removeChain(chain);
        return dto;
    }

    public static TauMemberSettingsResponseDTO setShowStatus(ConsoleContext context, boolean b) throws Exception {
        TauMemberSettingsClientChain chain = new TauMemberSettingsClientChain(context.client);
        context.io.chainManager.linkChain(chain);
        TauMemberSettingsResponseDTO dto = chain.setShowStatus(b);
        context.io.chainManager.removeChain(chain);
        return dto;
    }
}
