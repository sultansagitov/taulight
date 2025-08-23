package net.result.main.commands.taulight;

import net.result.main.commands.ConsoleContext;
import net.result.taulight.chain.sender.TauMemberSettingsClientChain;
import net.result.taulight.dto.TauMemberSettingsDTO;

public class SettingsRunner {
    public static TauMemberSettingsDTO get(ConsoleContext context) {
        TauMemberSettingsClientChain chain = new TauMemberSettingsClientChain(context.client);
        context.io.chainManager.linkChain(chain);
        TauMemberSettingsDTO dto = chain.get();
        context.io.chainManager.removeChain(chain);
        return dto;
    }

    public static TauMemberSettingsDTO setShowStatus(ConsoleContext context, boolean b) {
        TauMemberSettingsClientChain chain = new TauMemberSettingsClientChain(context.client);
        context.io.chainManager.linkChain(chain);
        TauMemberSettingsDTO dto = chain.setShowStatus(b);
        context.io.chainManager.removeChain(chain);
        return dto;
    }
}
