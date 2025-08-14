package net.result.taulight.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TauMemberSettingsDTO {
    @JsonProperty("show-status")
    public boolean showStatus;

    @SuppressWarnings("unused")
    public TauMemberSettingsDTO() {
    }

    public TauMemberSettingsDTO(boolean showStatus) {
        this.showStatus = showStatus;
    }
}
