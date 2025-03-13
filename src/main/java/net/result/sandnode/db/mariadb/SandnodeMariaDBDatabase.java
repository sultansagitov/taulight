package net.result.sandnode.db.mariadb;

import net.result.sandnode.config.MariaDBConfig;
import net.result.sandnode.db.Database;
import net.result.sandnode.db.Member;
import net.result.sandnode.exception.error.BusyNicknameException;
import net.result.sandnode.exception.DatabaseException;
import net.result.sandnode.security.PasswordHasher;
import net.result.sandnode.util.UUIDUtil;
import org.jetbrains.annotations.NotNull;
import org.mariadb.jdbc.MariaDbDataSource;

import javax.sql.DataSource;
import java.sql.*;
import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.UUID;

public class SandnodeMariaDBDatabase implements Database {
    protected final DataSource dataSource;
    private final PasswordHasher hasher;

    public SandnodeMariaDBDatabase(MariaDBConfig mariaDBConfig, PasswordHasher hasher) throws DatabaseException {
        MariaDbDataSource dataSource = new MariaDbDataSource();
        try {
            dataSource.setUrl(mariaDBConfig.getURL());
            dataSource.setUser(mariaDBConfig.getUser());
            dataSource.setPassword(mariaDBConfig.getPassword());
        } catch (SQLException e) {
            throw new DatabaseException("Failed to connect %s".formatted(mariaDBConfig.getURL()), e);
        }

        this.dataSource = dataSource;
        this.hasher = hasher;
        try (Connection conn = dataSource.getConnection(); Statement stmt = conn.createStatement()) {
            initTables(stmt);
        } catch (SQLException e) {
            throw new DatabaseException("Failed to initialize database tables", e);
        }
    }

    @Override
    public synchronized Member registerMember(String nickname, String password)
            throws BusyNicknameException, DatabaseException {
        try (
                Connection conn = dataSource.getConnection();
                PreparedStatement checkStmt = conn.prepareStatement("SELECT 1 FROM members WHERE nickname = ?");
                PreparedStatement insertStmt = conn.prepareStatement("""
                    INSERT INTO members (member_id, created_at, nickname, password_hash)
                    VALUES (?, ?, ?, ?)
                """)
        ) {
            checkStmt.setString(1, nickname);
            try (ResultSet rs = checkStmt.executeQuery()) {
                if (rs.next()) throw new BusyNicknameException();
            }

            String passwordHash = hasher.hash(password);

            Member member = new Member(nickname, password);

            byte[] chatBin = UUIDUtil.uuidToBinary(member.id());

            insertStmt.setBytes(1, chatBin);
            insertStmt.setTimestamp(2, Timestamp.from(member.getCreationDate().toInstant()));

            insertStmt.setString(3, nickname);
            insertStmt.setString(4, passwordHash);
            insertStmt.executeUpdate();

            return member;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to register member", e);
        }
    }

    @Override
    public synchronized Optional<Member> findMemberByNickname(String nickname) throws DatabaseException {
        try (
                Connection conn = dataSource.getConnection();
                PreparedStatement stmt = conn.prepareStatement("""
                    SELECT member_id, created_at, password_hash FROM members WHERE nickname = ?
                """)
        ) {

            stmt.setString(1, nickname);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    byte[] uuidBin = rs.getBytes("member_id");

                    ZonedDateTime createdAt = rs
                            .getTimestamp("created_at").toInstant()
                            .atZone(ZonedDateTime.now().getZone());

                    UUID id = UUIDUtil.binaryToUUID(uuidBin);

                    String passwordHash = rs.getString("password_hash");
                    return Optional.of(new Member(id, createdAt, nickname, passwordHash));
                }
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new DatabaseException("Failed to find member", e);
        }
    }

    protected void initTables(@NotNull Statement stmt) throws SQLException {
        stmt.execute("""
            CREATE TABLE IF NOT EXISTS members (
                member_id BINARY(16) UNIQUE,
                created_at TIMESTAMP NOT NULL,
                nickname VARCHAR(255) UNIQUE,
                password_hash VARCHAR(255) NOT NULL,
                PRIMARY KEY (member_id, nickname)
            )
        """);
    }

    @Override
    public PasswordHasher hasher() {
        return hasher;
    }
}
