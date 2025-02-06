package net.result.taulight.db;

import net.result.sandnode.db.Member;
import net.result.sandnode.db.StandardMember;
import net.result.sandnode.exception.BusyMemberIDException;
import javax.sql.DataSource;
import java.sql.*;
import java.time.ZonedDateTime;
import java.util.*;

public class MariaDBDatabase implements TauDatabase {
    private final DataSource dataSource;

    public MariaDBDatabase(DataSource dataSource) {
        this.dataSource = dataSource;
        initializeTables();
    }

    private void initializeTables() {
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {

            // Members table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS members (
                    member_id VARCHAR(255) PRIMARY KEY,
                    password VARCHAR(255) NOT NULL
                )
            """);

            // Base chats table
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS chats (
                    chat_id VARCHAR(255) PRIMARY KEY
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

        } catch (SQLException e) {
            throw new RuntimeException("Failed to initialize database tables", e);
        }
    }

    @Override
    public synchronized Member registerMember(String memberID, String password) throws BusyMemberIDException {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement checkStmt = conn.prepareStatement("SELECT 1 FROM members WHERE member_id = ?");
             PreparedStatement insertStmt = conn.prepareStatement("INSERT INTO members (member_id, password) VALUES (?, ?)")) {

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
            throw new RuntimeException("Failed to register member", e);
        }
    }

    @Override
    public synchronized Optional<Member> findMemberByMemberID(String memberID) {
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
            throw new RuntimeException("Failed to find member", e);
        }
    }

    @Override
    public void saveChat(TauChat chat) {
        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // Insert into base chats table
                try (PreparedStatement stmt = conn.prepareStatement(
                        "INSERT INTO chats (chat_id) VALUES (?) ON DUPLICATE KEY UPDATE chat_id = chat_id")) {
                    stmt.setString(1, chat.getID());
                    stmt.executeUpdate();
                }

                // If it's a channel, handle the channel-specific data
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
            // First check if it's a channel
            try (PreparedStatement stmt = conn.prepareStatement(
                    "SELECT c.chat_id, ch.title, ch.owner_id FROM chats c " +
                            "LEFT JOIN channels ch ON c.chat_id = ch.chat_id " +
                            "WHERE c.chat_id = ?")) {

                stmt.setString(1, id);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        String title = rs.getString("title");
                        String ownerId = rs.getString("owner_id");

                        if (title != null && ownerId != null) {
                            // It's a channel
                            Member owner = findMemberByMemberID(ownerId).orElseThrow();
                            return Optional.of(new TauChannel(title, id, owner));
                        } else {
                            // It's a regular chat
                            return Optional.of(new TauChat(id));
                        }
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
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
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
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT c.chat_id, ch.title, ch.owner_id " +
                             "FROM chats c " +
                             "JOIN chat_members cm ON c.chat_id = cm.chat_id " +
                             "LEFT JOIN channels ch ON c.chat_id = ch.chat_id " +
                             "WHERE cm.member_id = ?")) {

            stmt.setString(1, member.getID());
            List<TauChat> chats = new ArrayList<>();
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String chatId = rs.getString("chat_id");
                    String title = rs.getString("title");
                    String ownerId = rs.getString("owner_id");

                    if (title != null && ownerId != null) {
                        // It's a channel
                        Member owner = findMemberByMemberID(ownerId).orElseThrow();
                        chats.add(new TauChannel(title, chatId, owner));
                    } else {
                        // It's a regular chat
                        chats.add(new TauChat(chatId));
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