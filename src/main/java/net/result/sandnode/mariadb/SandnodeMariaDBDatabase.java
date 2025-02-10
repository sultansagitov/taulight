package net.result.sandnode.mariadb;

import net.result.sandnode.db.Database;
import net.result.sandnode.db.Member;
import net.result.sandnode.db.StandardMember;
import net.result.sandnode.exception.BusyMemberIDException;
import net.result.sandnode.exception.DatabaseException;

import javax.sql.DataSource;
import java.sql.*;
import java.util.Optional;

public class SandnodeMariaDBDatabase implements Database {
    protected final DataSource dataSource;

    public SandnodeMariaDBDatabase(DataSource dataSource) throws DatabaseException {
        this.dataSource = dataSource;
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            initTables(stmt);
        } catch (SQLException e) {
            throw new DatabaseException("Failed to initialize database tables", e);
        }
    }

    @Override
    public synchronized Member registerMember(String memberID, String password)
            throws BusyMemberIDException, DatabaseException {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement checkStmt = conn.prepareStatement("SELECT 1 FROM members WHERE member_id = ?");
             PreparedStatement insertStmt =
                     conn.prepareStatement("INSERT INTO members (member_id, password) VALUES (?, ?)")) {

            checkStmt.setString(1, memberID);
            try (ResultSet rs = checkStmt.executeQuery()) {
                if (rs.next()) {
                    throw new BusyMemberIDException(memberID);
                }
            }

            insertStmt.setString(1, memberID);
            insertStmt.setString(2, password);
            insertStmt.executeUpdate();

            return new StandardMember(memberID, password, this);
        } catch (SQLException e) {
            throw new DatabaseException("Failed to register member", e);
        }
    }

    @Override
    public synchronized Optional<Member> findMemberByMemberID(String memberID) throws DatabaseException {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT password FROM members WHERE member_id = ?")) {

            stmt.setString(1, memberID);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String password = rs.getString("password");
                    return Optional.of(new StandardMember(memberID, password, this));
                }
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new DatabaseException("Failed to find member", e);
        }
    }

    protected void initTables(Statement stmt) throws SQLException {
        // Members table
        stmt.execute("""
            CREATE TABLE IF NOT EXISTS members (
                member_id VARCHAR(255) PRIMARY KEY,
                password VARCHAR(255) NOT NULL
            )
        """);
    }
}
