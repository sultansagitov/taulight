package net.result.taulight.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import net.result.taulight.db.TauMemberEntity;

public class TauMemberSettingsResponseDTO {
    @JsonProperty("show-status")
    public boolean showStatus;

    @SuppressWarnings("unused")
    public TauMemberSettingsResponseDTO() {
    }

    public TauMemberSettingsResponseDTO(boolean showStatus) {
        this.showStatus = showStatus;
    }

    public TauMemberSettingsResponseDTO(TauMemberEntity entity) {
        this(entity.isShowStatus());
    }
}
