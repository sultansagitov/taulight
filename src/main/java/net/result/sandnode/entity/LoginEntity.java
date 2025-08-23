package net.result.sandnode.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.result.sandnode.dto.LoginHistoryDTO;

/**
 * Represents a login session for a {@link MemberEntity}. Each login entry stores the IP address
 * and device information. If {@code login} is not {@code null}, it indicates that this login
 * was performed using a token that was issued during a previous login (which may have originated
 * from a password-based login or a registration).
 */
@Setter
@Getter
@NoArgsConstructor
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
    private KeyStorageEntity encryptor;

    @ManyToOne
    private MemberEntity member;

    public LoginEntity(String ip, String device, LoginEntity login, KeyStorageEntity encryptor, MemberEntity member) {
        this.encryptor = encryptor;
        this.member = member;
        this.login = login;
        this.ip = ip;
        this.device = device;
    }

    /**
     * Constructs a new login entry using a previous login (typically from a token).
     * Copies the member and device from the original login.
     *
     * @param login the original login entity (token source)
     * @param ip    the IP address of the new login
     */
    public LoginEntity(LoginEntity login, String ip) {
        setLogin(login);
        setMember(login.getMember());
        setIp(ip);
        setDevice(login.getDevice());
    }

    public LoginHistoryDTO toDTO(boolean isOnline) {
        return new LoginHistoryDTO(getCreationDate(), getIp(), getDevice(), isOnline);
    }
}
