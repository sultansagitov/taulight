package net.result.sandnode.db;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;

@SuppressWarnings("unused")
@Entity
public class LoginEntity extends BaseEntity {
    private String ip;
    private boolean byPassword;

    @ManyToOne
    private MemberEntity member;

    public LoginEntity() {}

    public LoginEntity(MemberEntity member, String ip, boolean byPassword) {
        super();
        setMember(member);
        setIp(ip);
        setByPassword(byPassword);
    }

    public String ip() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
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
