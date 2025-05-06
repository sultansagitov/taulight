package net.result.sandnode.db;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;

@SuppressWarnings("unused")
@Entity
public class LoginEntity extends SandnodeEntity {
    private boolean byPassword;

    @ManyToOne
    private MemberEntity member;

    public LoginEntity() {}

    public LoginEntity(MemberEntity member, boolean byPassword) {
        super();
        setMember(member);
        setByPassword(byPassword);
    }

    public boolean byPassword() {
        return byPassword;
    }

    public void setByPassword(boolean byPassword) {
        this.byPassword = byPassword;
    }

    public MemberEntity member() {
        return member;
    }

    public void setMember(MemberEntity member) {
        this.member = member;
    }
}
