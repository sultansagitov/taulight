package net.result.taulight.db;

import jakarta.persistence.Entity;
import net.result.sandnode.db.BaseEntity;

@Entity
public class TauMemberSettingsEntity extends BaseEntity {
    private boolean showStatus;

    public TauMemberSettingsEntity() {
        super();
        setShowStatus(false);
    }

    public boolean isShowStatus() {
        return showStatus;
    }

    public void setShowStatus(boolean showStatus) {
        this.showStatus = showStatus;
    }
}
