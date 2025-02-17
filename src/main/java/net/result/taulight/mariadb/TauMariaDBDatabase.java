package net.result.taulight.mariadb;

import net.result.sandnode.db.Member;
import net.result.sandnode.db.StandardMember;
import net.result.sandnode.exception.DatabaseException;
import net.result.sandnode.mariadb.SandnodeMariaDBDatabase;
import net.result.taulight.db.*;

import javax.sql.DataSource;
import java.nio.ByteBuffer;
import java.sql.*;
import java.time.ZonedDateTime;
import java.util.*;

public class TauMariaDBDatabase extends SandnodeMariaDBDatabase implements TauDatabase {
    public TauMariaDBDatabase(DataSource dataSource) throws DatabaseException {
        super(dataSource);
    }

    private byte[] uuidToBinary(UUID uuid) {
        ByteBuffer bb = ByteBuffer.wrap(new byte[16]);
        bb.putLong(uuid.getMostSignificantBits());
        bb.putLong(uuid.getLeastSignificantBits());
        return bb.array();
    }

    private UUID binaryToUUID(byte[] bytes) {
        ByteBuffer bb = ByteBuffer.wrap(bytes);
        long firstLong = bb.getLong();
        long secondLong = bb.getLong();
        return new UUID(firstLong, secondLong);
    }

    @Override
    public void initTables(Statement stmt) throws SQLException {
        super.initTables(stmt);

        // Base chats table
        stmt.execute("""
            CREATE TABLE IF NOT EXISTS chats (
                chat_id BINARY(16) PRIMARY KEY,
                chat_type ENUM('CHANNEL', 'DIRECT') NOT NULL
            )
        """);

        // Channels table extending chats
        stmt.execute("""
            CREATE TABLE IF NOT EXISTS channels (
                chat_id BINARY(16) PRIMARY KEY,
                title VARCHAR(255) NOT NULL,
                owner_id VARCHAR(255) NOT NULL,
                FOREIGN KEY (chat_id) REFERENCES chats(chat_id) ON DELETE CASCADE,
                FOREIGN KEY (owner_id) REFERENCES members(member_id)
            )
        """);

        // Chat members (many-to-many relationship)
        stmt.execute("""
            CREATE TABLE IF NOT EXISTS chat_members (
                chat_id BINARY(16),
                member_id VARCHAR(255),
                PRIMARY KEY (chat_id, member_id),
                FOREIGN KEY (chat_id) REFERENCES chats(chat_id) ON DELETE CASCADE,
                FOREIGN KEY (member_id) REFERENCES members(member_id)
            )
        """);

        // Messages table
        stmt.execute("""
            CREATE TABLE IF NOT EXISTS messages (
                message_id BINARY(16) PRIMARY KEY,
                chat_id BINARY(16),
                content TEXT NOT NULL,
                timestamp TIMESTAMP NOT NULL,
                member_id VARCHAR(255),
                sys BOOLEAN,
                FOREIGN KEY (chat_id) REFERENCES chats(chat_id) ON DELETE CASCADE,
                FOREIGN KEY (member_id) REFERENCES members(member_id)
            )
        """);

        // Direct chats
        stmt.execute("""
            CREATE TABLE IF NOT EXISTS direct_chats (
                chat_id BINARY(16) PRIMARY KEY,
                member1_id VARCHAR(255) NOT NULL,
                member2_id VARCHAR(255) NOT NULL,
                FOREIGN KEY (chat_id) REFERENCES chats(chat_id) ON DELETE CASCADE,
                FOREIGN KEY (member1_id) REFERENCES members(member_id),
                FOREIGN KEY (member2_id) REFERENCES members(member_id),
                UNIQUE KEY (member1_id, member2_id)
            )
        """);
    }

    @Override
    public TauDirect createDirectChat(Member member1, Member member2) throws DatabaseException {
        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);
            try {
                TauDirect direct = new TauDirect(member1, member2);

                byte[] chatBin = uuidToBinary(direct.getID());

                try (PreparedStatement stmt = conn.prepareStatement(
                        "INSERT INTO chats (chat_id, chat_type) VALUES (?, 'DIRECT')")) {
                    stmt.setBytes(1, chatBin);
                    stmt.executeUpdate();
                }

                try (PreparedStatement stmt = conn.prepareStatement(
                        "INSERT INTO direct_chats (chat_id, member1_id, member2_id) VALUES (?, ?, ?)")) {
                    stmt.setBytes(1, chatBin);
                    stmt.setString(2, member1.getID());
                    stmt.setString(3, member2.getID());
                    stmt.executeUpdate();
                }

                conn.commit();
                return direct;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to create direct chat", e);
        }
    }

    @Override
    public Optional<TauDirect> findDirectChat(Member member1, Member member2) throws DatabaseException {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT chat_id FROM direct_chats " +
                             "WHERE (member1_id = ? AND member2_id = ?) " +
                             "   OR (member1_id = ? AND member2_id = ?)")) {

            stmt.setString(1, member1.getID());
            stmt.setString(2, member2.getID());
            stmt.setString(3, member2.getID());
            stmt.setString(4, member1.getID());

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    byte[] uuidBin = rs.getBytes("chat_id");
                    UUID chatID = binaryToUUID(uuidBin);
                    return Optional.of(new TauDirect(chatID, member1, member2));
                }
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new DatabaseException("Failed to find direct chat", e);
        }
    }

    @Override
    public void saveChat(TauChat chat) throws DatabaseException {
        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);
            try {
                byte[] uuidBin = uuidToBinary(chat.getID());

                try (PreparedStatement checkStmt = conn.prepareStatement(
                        "INSERT IGNORE INTO chats (chat_id, chat_type) VALUES (?, ?)")) {
                    checkStmt.setBytes(1, uuidBin);
                    if (chat instanceof TauChannel) {
                        checkStmt.setString(2, "CHANNEL");
                    } else {
                        checkStmt.setString(2, "DIRECT");
                    }
                    checkStmt.executeUpdate();
                }

                if (chat instanceof TauChannel channel) {
                    try (PreparedStatement stmt = conn.prepareStatement(
                            "INSERT INTO channels (chat_id, title, owner_id) VALUES (?, ?, ?) " +
                                    "ON DUPLICATE KEY UPDATE title = ?, owner_id = ?")) {
                        stmt.setBytes(1, uuidBin);
                        stmt.setString(2, channel.getTitle());
                        stmt.setString(3, channel.getOwner().getID());
                        stmt.setString(4, channel.getTitle());
                        stmt.setString(5, channel.getOwner().getID());
                        stmt.executeUpdate();
                    }
                }

                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException | IllegalArgumentException e) {
            throw new DatabaseException("Failed to save chat", e);
        }
    }

    @Override
    public Optional<TauChat> getChat(UUID id) throws DatabaseException {
        try (Connection conn = dataSource.getConnection()) {
            try {
                byte[] chatID = uuidToBinary(id);

                try (PreparedStatement typeStmt = conn.prepareStatement(
                        "SELECT chat_type FROM chats WHERE chat_id = ?")) {
                    typeStmt.setBytes(1, chatID);
                    try (ResultSet rs = typeStmt.executeQuery()) {
                        if (rs.next()) {
                            String chatType = rs.getString("chat_type");

                            if ("CHANNEL".equals(chatType)) {
                                return getChannelChat(conn, id);
                            } else if ("DIRECT".equals(chatType)) {
                                return getDirectChat(conn, id);
                            }
                        }
                    }
                }
                return Optional.empty();
            } catch (IllegalArgumentException e) {
                // Not a valid UUID
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to get chat", e);
        }
    }

    private Optional<TauChat> getChannelChat(Connection conn, UUID id) throws SQLException, DatabaseException {
        try (PreparedStatement channelStmt = conn.prepareStatement(
                "SELECT title, owner_id FROM channels WHERE chat_id = ?")) {
            byte[] chatID = uuidToBinary(id);
            channelStmt.setBytes(1, chatID);
            try (ResultSet rs = channelStmt.executeQuery()) {
                if (rs.next()) {
                    String title = rs.getString("title");
                    String ownerMemberID = rs.getString("owner_id");
                    Member owner = findMemberByMemberID(ownerMemberID).orElseThrow();
                    return Optional.of(new TauChannel(id, title, owner));
                }
            }
        }
        return Optional.empty();
    }

    private Optional<TauChat> getDirectChat(Connection conn, UUID id) throws SQLException, DatabaseException {
        try (PreparedStatement directStmt = conn.prepareStatement(
                "SELECT member1_id, member2_id FROM direct_chats WHERE chat_id = ?")) {
            byte[] chatID = uuidToBinary(id);
            directStmt.setBytes(1, chatID);
            try (ResultSet rs = directStmt.executeQuery()) {
                if (rs.next()) {
                    Member member1 = findMemberByMemberID(rs.getString("member1_id")).orElseThrow();
                    Member member2 = findMemberByMemberID(rs.getString("member2_id")).orElseThrow();
                    return Optional.of(new TauDirect(id, member1, member2));
                }
            }
        }
        return Optional.empty();
    }

    @Override
    public void saveMessage(ChatMessage msg) throws DatabaseException {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "INSERT INTO messages (message_id, chat_id, content, timestamp, member_id, sys)" +
                     "VALUES (?, ?, ?, ?, ?, ?)")) {

            stmt.setBytes(1, uuidToBinary(msg.id()));
            stmt.setBytes(2, uuidToBinary(msg.chatID()));
            stmt.setString(3, msg.content());
            stmt.setTimestamp(4, Timestamp.from(msg.ztd().toInstant()));
            stmt.setString(5, msg.memberID());
            stmt.setBoolean(6, msg.sys());
            stmt.executeUpdate();
        } catch (SQLException | IllegalArgumentException e) {
            throw new DatabaseException("Failed to save message", e);
        }
    }

    @Override
    public Collection<ChatMessage> loadMessages(TauChat chat, int index, int size) throws DatabaseException {
        try (Connection conn = dataSource.getConnection()) {
            byte[] chatBin = uuidToBinary(chat.getID());

            try (PreparedStatement stmt = conn.prepareStatement(
                    "SELECT message_id, content, timestamp, member_id FROM messages " +
                            "WHERE chat_id = ? ORDER BY timestamp DESC LIMIT ? OFFSET ?")) {

                stmt.setBytes(1, chatBin);
                stmt.setInt(2, size);
                stmt.setInt(3, index);

                List<ChatMessage> messages = new ArrayList<>();
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        byte[] messageBin = rs.getBytes("message_id");
                        UUID messageID = binaryToUUID(messageBin);

                        ZonedDateTime timestamp = rs.getTimestamp("timestamp")
                                .toInstant()
                                .atZone(ZonedDateTime.now().getZone());

                        ChatMessage message = new ChatMessage()
                                .setID(messageID)
                                .setChat(chat)
                                .setContent(rs.getString("content"))
                                .setZtd(timestamp)
                                .setMemberID(rs.getString("member_id"))
                                .setSys(rs.getBoolean("sys"));

                        messages.add(message);
                    }
                }
                return messages;
            }
        } catch (SQLException | IllegalArgumentException e) {
            throw new DatabaseException("Failed to load messages", e);
        }
    }

    @Override
    public Collection<Member> getMembersFromChat(TauChat chat) throws DatabaseException {
        try (Connection conn = dataSource.getConnection()) {
            byte[] chatID = uuidToBinary(chat.getID());

            try (PreparedStatement typeStmt = conn.prepareStatement(
                    "SELECT chat_type FROM chats WHERE chat_id = ?")) {
                typeStmt.setBytes(1, chatID);

                try (ResultSet typeRs = typeStmt.executeQuery()) {
                    if (typeRs.next() && "DIRECT".equals(typeRs.getString("chat_type"))) {
                        return getDirectChatMembers(conn, chatID);
                    }
                }
            }

            return getChannelMembers(conn, chatID);
        } catch (SQLException | IllegalArgumentException e) {
            throw new DatabaseException("Failed to get chat members", e);
        }
    }

    private List<Member> getDirectChatMembers(Connection conn, byte[] chatID) throws SQLException, DatabaseException {
        try (PreparedStatement stmt = conn.prepareStatement(
                "SELECT member1_id, member2_id FROM direct_chats WHERE chat_id = ?")) {
            stmt.setBytes(1, chatID);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    List<Member> members = new ArrayList<>(2);
                    members.add(findMemberByMemberID(rs.getString("member1_id")).orElseThrow());
                    members.add(findMemberByMemberID(rs.getString("member2_id")).orElseThrow());
                    return members;
                }
            }
        }
        return List.of();
    }

    private List<Member> getChannelMembers(Connection conn, byte[] chatID) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement(
                "SELECT m.member_id, m.password FROM members m " +
                        "JOIN chat_members cm ON m.member_id = cm.member_id " +
                        "WHERE cm.chat_id = ?")) {
            stmt.setBytes(1, chatID);
            List<Member> members = new ArrayList<>();
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    members.add(new StandardMember(
                            rs.getString("member_id"),
                            rs.getString("password")
                    ));
                }
            }
            return members;
        }
    }

    @Override
    public void addMemberToChat(TauChat chat, Member member) throws DatabaseException {
        try (Connection conn = dataSource.getConnection()) {
            byte[] chatID = uuidToBinary(chat.getID());

            try (PreparedStatement stmt = conn.prepareStatement(
                    "INSERT INTO chat_members (chat_id, member_id) VALUES (?, ?)")) {

                stmt.setBytes(1, chatID);
                stmt.setString(2, member.getID());
                stmt.executeUpdate();
            }
        } catch (SQLException | IllegalArgumentException e) {
            throw new DatabaseException("Failed to add member to chat", e);
        }
    }

    @Override
    public void leaveFromChat(TauChannel channel, Member member) throws DatabaseException {
        try (Connection conn = dataSource.getConnection()) {
            byte[] chatID = uuidToBinary(channel.getID());

            conn.setAutoCommit(false);
            try {
                try (PreparedStatement stmt = conn.prepareStatement(
                        "DELETE FROM chat_members WHERE chat_id = ? AND member_id = ?")) {
                    stmt.setBytes(1, chatID);
                    stmt.setString(2, member.getID());
                    stmt.executeUpdate();
                }

                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw new DatabaseException("Failed to leave chat", e);
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException | IllegalArgumentException e) {
            throw new DatabaseException("Database error while leaving chat", e);
        }
    }

    @Override
    public Collection<TauChat> getChats(Member member) throws DatabaseException {
        try (Connection conn = dataSource.getConnection()) {
            List<TauChat> chats = new ArrayList<>();

            // Get channels
            try (PreparedStatement stmt = conn.prepareStatement(
                    "SELECT c.chat_id, ch.title, ch.owner_id " +
                            "FROM chats c " +
                            "JOIN chat_members cm ON c.chat_id = cm.chat_id " +
                            "JOIN channels ch ON c.chat_id = ch.chat_id " +
                            "WHERE cm.member_id = ? AND c.chat_type = 'CHANNEL'")) {

                stmt.setString(1, member.getID());
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        byte[] chatBin = rs.getBytes("chat_id");
                        UUID chatID = binaryToUUID(chatBin);
                        String title = rs.getString("title");
                        String ownerMemberID = rs.getString("owner_id");
                        Member owner = findMemberByMemberID(ownerMemberID).orElseThrow();
                        chats.add(new TauChannel(chatID, title, owner));
                    }
                }
            }

            // Get direct chats
            try (PreparedStatement stmt = conn.prepareStatement(
                    "SELECT c.chat_id, " +
                            "CASE WHEN dc.member1_id = ? " +
                            "THEN dc.member2_id " +
                            "ELSE dc.member1_id " +
                            "END AS other_member_id " +
                            "FROM chats c " +
                            "JOIN direct_chats dc " +
                            "ON c.chat_id = dc.chat_id " +
                            "WHERE c.chat_type = 'DIRECT' AND (dc.member1_id = ? OR dc.member2_id = ?)")) {

                stmt.setString(1, member.getID());
                stmt.setString(2, member.getID());
                stmt.setString(3, member.getID());

                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        byte[] chatBin = rs.getBytes("chat_id");
                        UUID chatID = binaryToUUID(chatBin);
                        String otherMemberID = rs.getString("other_member_id");
                        Member otherMember = findMemberByMemberID(otherMemberID).orElseThrow();
                        chats.add(new TauDirect(chatID, member, otherMember));
                    }
                }
            }

            return chats;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to get member chats", e);
        }
    }

    @Override
    public void removeChat(UUID chatID) throws DatabaseException {
        try (Connection conn = dataSource.getConnection()) {
            byte[] chatBin = uuidToBinary(chatID);

            try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM chats WHERE chat_id = ?")) {
                stmt.setBytes(1, chatBin);
                stmt.executeUpdate();
            }
        } catch (SQLException | IllegalArgumentException e) {
            throw new DatabaseException("Failed to remove chat", e);
        }
    }

    @Override
    public long getMessageCount(TauChat chat) throws DatabaseException {
        try (Connection conn = dataSource.getConnection()) {
            byte[] chatBin = uuidToBinary(chat.getID());

            try (PreparedStatement stmt = conn.prepareStatement("SELECT COUNT(*) FROM messages WHERE chat_id = ?")) {

                stmt.setBytes(1, chatBin);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return rs.getLong(1);
                    }
                }
            }
            return 0;
        } catch (SQLException | IllegalArgumentException e) {
            throw new DatabaseException("Failed to get message count", e);
        }
    }
}