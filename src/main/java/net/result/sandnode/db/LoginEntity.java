package net.result.sandnode.db;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;

/**
 * Represents a login session for a {@link MemberEntity}. Each login entry stores the IP address
 * and device information. If {@code login} is not {@code null}, it indicates that this login
 * was performed using a token that was issued during a previous login (which may have originated
 * from a password-based login or a registration).
 */
@SuppressWarnings("unused")
@Entity
public class LoginEntity extends BaseEntity {
    private String ip;
    private String device;

    /**
     * Refers to the original login from which a token was issued.
     * If not null, this login was created using that token.
     */
    @ManyToOne
    private LoginEntity login;

    @ManyToOne
    private MemberEntity member;

    public LoginEntity() {}

    /**
     * Constructs a new login entry for the given member with IP and device details.
     *
     * @param member the member associated with this login
     * @param ip     the IP address of the login
     * @param device the device used for login
     */
    public LoginEntity(MemberEntity member, String ip, String device) {
        super();
        setMember(member);
        setIp(ip);
        setDevice(device);
    }

    /**
     * Constructs a new login entry using a previous login (typically from a token).
     * Copies the member and device from the original login.
     *
     * @param login the original login entity (token source)
     * @param ip    the IP address of the new login
     */
    public LoginEntity(LoginEntity login, String ip) {
        super();
        setLogin(login);
        setMember(login.member());
        setIp(ip);
        setDevice(login.device());
    }

    public String ip() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String device() {
        return device;
    }

    public void setDevice(String device) {
        this.device = device;
    }

    public LoginEntity login() {
        return login;
    }

    public void setLogin(LoginEntity login) {
        this.login = login;
    }

    public MemberEntity member() {
        return member;
    }

    public void setMember(MemberEntity member) {
        this.member = member;
    }
}
