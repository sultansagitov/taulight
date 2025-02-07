package net.result.taulight.mariadb;

import net.result.sandnode.db.Member;
import net.result.sandnode.db.StandardMember;
import net.result.sandnode.mariadb.SandnodeMariaDBDatabase;
import net.result.taulight.db.*;

import javax.sql.DataSource;
import java.sql.*;
import java.time.ZonedDateTime;
import java.util.*;

public class TauMariaDBDatabase extends SandnodeMariaDBDatabase implements TauDatabase {
    public TauMariaDBDatabase(DataSource dataSource) {
        super(dataSource);
    }

    @Override
    public void initTables(Statement stmt) throws SQLException {
        super.initTables(stmt);

        // Base chats table
        stmt.execute("""
            CREATE TABLE IF NOT EXISTS chats (
                chat_id VARCHAR(255) PRIMARY KEY,
                chat_type ENUM('DIRECT', 'CHANNEL') NOT NULL
            )
        """);

        // Channels table extending chats
        stmt.execute("""
            CREATE TABLE IF NOT EXISTS channels (
                chat_id VARCHAR(255) PRIMARY KEY,
                title VARCHAR(255) NOT NULL,
                owner_id VARCHAR(255) NOT NULL,
                FOREIGN KEY (chat_id) REFERENCES chats(chat_id) ON DELETE CASCADE,
                FOREIGN KEY (owner_id) REFERENCES members(member_id)
            )
        """);

        // Chat members (many-to-many relationship)
        stmt.execute("""
            CREATE TABLE IF NOT EXISTS chat_members (
                chat_id VARCHAR(255),
                member_id VARCHAR(255),
                PRIMARY KEY (chat_id, member_id),
                FOREIGN KEY (chat_id) REFERENCES chats(chat_id) ON DELETE CASCADE,
                FOREIGN KEY (member_id) REFERENCES members(member_id)
            )
        """);

        // Messages table
        stmt.execute("""
            CREATE TABLE IF NOT EXISTS messages (
                message_id BIGINT AUTO_INCREMENT PRIMARY KEY,
                chat_id VARCHAR(255),
                content TEXT NOT NULL,
                timestamp TIMESTAMP NOT NULL,
                member_id VARCHAR(255),
                FOREIGN KEY (chat_id) REFERENCES chats(chat_id) ON DELETE CASCADE,
                FOREIGN KEY (member_id) REFERENCES members(member_id)
            )
        """);

        // Direct chats
        stmt.execute("""
            CREATE TABLE IF NOT EXISTS direct_chats (
                chat_id VARCHAR(255) PRIMARY KEY,
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
    public TauDirect createDirectChat(Member member1, Member member2) {
        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);
            try {
                String chatId = "direct_" + UUID.randomUUID();

                try (PreparedStatement stmt = conn.prepareStatement(
                        "INSERT INTO chats (chat_id, chat_type) VALUES (?, 'DIRECT')")) {
                    stmt.setString(1, chatId);
                    stmt.executeUpdate();
                }

                try (PreparedStatement stmt = conn.prepareStatement(
                        "INSERT INTO direct_chats (chat_id, member1_id, member2_id) VALUES (?, ?, ?)")) {
                    stmt.setString(1, chatId);
                    stmt.setString(2, member1.getID());
                    stmt.setString(3, member2.getID());
                    stmt.executeUpdate();
                }

                conn.commit();
                return new TauDirect(chatId, member1, member2);
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to create direct chat", e);
        }
    }

    @Override
    public Optional<TauDirect> findDirectChat(Member member1, Member member2) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT chat_id FROM direct_chats " +
                             "WHERE (member1_id = ? AND member2_id = ?) OR (member1_id = ? AND member2_id = ?)")) {

            stmt.setString(1, member1.getID());
            stmt.setString(2, member2.getID());
            stmt.setString(3, member2.getID());
            stmt.setString(4, member1.getID());

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String chatId = rs.getString("chat_id");
                    return Optional.of(new TauDirect(chatId, member1, member2));
                }
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to find direct chat", e);
        }
    }

    @Override
    public void saveChat(TauChat chat) {
        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);
            try {
                try (PreparedStatement checkStmt = conn.prepareStatement(
                        "INSERT IGNORE INTO chats (chat_id, chat_type) VALUES (?, 'CHANNEL')")) {
                    checkStmt.setString(1, chat.getID());
                    checkStmt.executeUpdate();
                }

                if (chat instanceof TauChannel channel) {
                    try (PreparedStatement stmt = conn.prepareStatement(
                            "INSERT INTO channels (chat_id, title, owner_id) VALUES (?, ?, ?) " +
                                    "ON DUPLICATE KEY UPDATE title = ?, owner_id = ?")) {
                        stmt.setString(1, channel.getID());
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
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save chat", e);
        }
    }

    @Override
    public Optional<TauChat> getChat(String id) {
        try (Connection conn = dataSource.getConnection()) {
            try (PreparedStatement channelStmt = conn.prepareStatement(
                    "SELECT c.chat_id, ch.title, ch.owner_id FROM chats c " +
                            "LEFT JOIN channels ch ON c.chat_id = ch.chat_id " +
                            "WHERE c.chat_id = ? AND c.chat_type = 'CHANNEL'")) {

                channelStmt.setString(1, id);
                try (ResultSet rs = channelStmt.executeQuery()) {
                    if (rs.next()) {
                        String title = rs.getString("title");
                        String ownerId = rs.getString("owner_id");
                        Member owner = findMemberByMemberID(ownerId).orElseThrow();
                        return Optional.of(new TauChannel(title, id, owner));
                    }
                }
            }

            try (PreparedStatement directStmt = conn.prepareStatement(
                    "SELECT dc.chat_id, dc.member1_id, dc.member2_id " +
                            "FROM chats c " +
                            "JOIN direct_chats dc ON c.chat_id = dc.chat_id " +
                            "WHERE c.chat_id = ? AND c.chat_type = 'DIRECT'")) {

                directStmt.setString(1, id);
                try (ResultSet rs = directStmt.executeQuery()) {
                    if (rs.next()) {
                        Member member1 = findMemberByMemberID(rs.getString("member1_id")).orElseThrow();
                        Member member2 = findMemberByMemberID(rs.getString("member2_id")).orElseThrow();
                        return Optional.of(new TauDirect(id, member1, member2));
                    }
                }
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to get chat", e);
        }
    }

    @Override
    public void saveMessage(ChatMessage msg) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "INSERT INTO messages (chat_id, content, timestamp, member_id) VALUES (?, ?, ?, ?)")) {

            stmt.setString(1, msg.chatID());
            stmt.setString(2, msg.content());
            stmt.setTimestamp(3, Timestamp.from(msg.ztd().toInstant()));
            stmt.setString(4, msg.memberID());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save message", e);
        }
    }

    @Override
    public Collection<ChatMessage> loadMessages(TauChat chat, int index, int size) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT content, timestamp, member_id FROM messages " +
                             "WHERE chat_id = ? ORDER BY timestamp DESC LIMIT ? OFFSET ?")) {

            stmt.setString(1, chat.getID());
            stmt.setInt(2, size);
            stmt.setInt(3, index);

            List<ChatMessage> messages = new ArrayList<>();
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    messages.add(new ChatMessage(
                            chat.getID(),
                            rs.getString("content"),
                            rs.getTimestamp("timestamp").toInstant().atZone(ZonedDateTime.now().getZone()),
                            rs.getString("member_id")
                    ));
                }
            }
            return messages;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load messages", e);
        }
    }

    @Override
    public Collection<Member> getMembersFromChat(TauChat chat) {
        try (Connection conn = dataSource.getConnection()) {
            try (PreparedStatement stmt = conn.prepareStatement(
                    "SELECT member1_id, member2_id FROM direct_chats WHERE chat_id = ?")) {
                stmt.setString(1, chat.getID());
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        List<Member> members = new ArrayList<>(2);
                        members.add(findMemberByMemberID(rs.getString("member1_id")).orElseThrow());
                        members.add(findMemberByMemberID(rs.getString("member2_id")).orElseThrow());
                        return members;
                    }
                }
            }

            try (PreparedStatement stmt = conn.prepareStatement(
                    "SELECT m.member_id, m.password FROM members m " +
                            "JOIN chat_members cm ON m.member_id = cm.member_id " +
                            "WHERE cm.chat_id = ?")) {

                stmt.setString(1, chat.getID());
                List<Member> members = new ArrayList<>();
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        members.add(new StandardMember(
                                rs.getString("member_id"),
                                rs.getString("password"),
                                this
                        ));
                    }
                }
                return members;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to get chat members", e);
        }
    }

    @Override
    public void addMemberToChat(TauChat chat, Member member) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "INSERT INTO chat_members (chat_id, member_id) VALUES (?, ?)")) {

            stmt.setString(1, chat.getID());
            stmt.setString(2, member.getID());
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to add member to chat", e);
        }
    }

    @Override
    public Collection<TauChat> getChats(Member member) {
        try (Connection conn = dataSource.getConnection()) {
            List<TauChat> chats = new ArrayList<>();

            try (PreparedStatement stmt = conn.prepareStatement(
                    "SELECT c.chat_id, ch.title, ch.owner_id " +
                            "FROM chats c " +
                            "JOIN chat_members cm ON c.chat_id = cm.chat_id " +
                            "LEFT JOIN channels ch ON c.chat_id = ch.chat_id " +
                            "WHERE cm.member_id = ? AND c.chat_type = 'CHANNEL'")) {

                stmt.setString(1, member.getID());
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        String chatId = rs.getString("chat_id");
                        String title = rs.getString("title");
                        String ownerId = rs.getString("owner_id");
                        Member owner = findMemberByMemberID(ownerId).orElseThrow();
                        chats.add(new TauChannel(title, chatId, owner));
                    }
                }
            }

            try (PreparedStatement stmt = conn.prepareStatement(
                    "SELECT chat_id, " +
                            "CASE WHEN member1_id = ? THEN member2_id ELSE member1_id END AS other_member_id " +
                            "FROM direct_chats " +
                            "WHERE member1_id = ? OR member2_id = ?")) {

                stmt.setString(1, member.getID());
                stmt.setString(2, member.getID());
                stmt.setString(3, member.getID());

                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        String chatId = rs.getString("chat_id");
                        String otherMemberId = rs.getString("other_member_id");
                        Member otherMember = findMemberByMemberID(otherMemberId).orElseThrow();
                        chats.add(new TauDirect(chatId, member, otherMember));
                    }
                }
            }

            return chats;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to get member chats", e);
        }
    }

    @Override
    public void removeChat(String chatId) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement("DELETE FROM chats WHERE chat_id = ?")) {

            stmt.setString(1, chatId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to remove chat", e);
        }
    }

    @Override
    public long getMessageCount(TauChat chat) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT COUNT(*) FROM messages WHERE chat_id = ?")) {

            stmt.setString(1, chat.getID());
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong(1);
                }
            }
            return 0;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to get message count", e);
        }
    }
}