package net.result.sandnode.mariadb;

import net.result.sandnode.db.Database;
import net.result.sandnode.db.Member;
import net.result.sandnode.db.StandardMember;
import net.result.sandnode.exception.BusyMemberIDException;
import net.result.sandnode.exception.DatabaseException;
import net.result.sandnode.security.PasswordHasher;
import org.jetbrains.annotations.NotNull;

import javax.sql.DataSource;
import java.sql.*;
import java.util.Optional;

public class SandnodeMariaDBDatabase implements Database {
    protected final DataSource dataSource;
    private final PasswordHasher hasher;

    public SandnodeMariaDBDatabase(DataSource dataSource, PasswordHasher hasher) throws DatabaseException {
        this.dataSource = dataSource;
        this.hasher = hasher;
        try (var conn = dataSource.getConnection(); var stmt = conn.createStatement()) {
            initTables(stmt);
        } catch (SQLException e) {
            throw new DatabaseException("Failed to initialize database tables", e);
        }
    }

    @Override
    public synchronized Member registerMember(String memberID, String password)
            throws BusyMemberIDException, DatabaseException {
        try (
                var conn = dataSource.getConnection();
                var checkStmt = conn.prepareStatement("SELECT 1 FROM members WHERE member_id = ?");
                var insertStmt
                        = conn.prepareStatement("INSERT INTO members (member_id, password_hash) VALUES (?, ?)")
        ) {
            checkStmt.setString(1, memberID);
            try (ResultSet rs = checkStmt.executeQuery()) {
                if (rs.next()) throw new BusyMemberIDException();
            }

            String passwordHash = hasher.hash(password);

            insertStmt.setString(1, memberID);
            insertStmt.setString(2, passwordHash);
            insertStmt.executeUpdate();

            return new StandardMember(memberID, password);
        } catch (SQLException e) {
            throw new DatabaseException("Failed to register member", e);
        }
    }

    @Override
    public synchronized Optional<Member> findMemberByMemberID(String memberID) throws DatabaseException {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT password_hash FROM members WHERE member_id = ?")) {

            stmt.setString(1, memberID);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String passwordHash = rs.getString("password_hash");
                    return Optional.of(new StandardMember(memberID, passwordHash));
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
                member_id VARCHAR(255) PRIMARY KEY,
                password_hash VARCHAR(255) NOT NULL
            )
        """);
    }

    @Override
    public PasswordHasher hasher() {
        return hasher;
    }
}
