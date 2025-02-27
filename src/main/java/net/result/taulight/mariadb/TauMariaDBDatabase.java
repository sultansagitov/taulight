package net.result.taulight.mariadb;

import net.result.sandnode.db.Member;
import net.result.sandnode.db.StandardMember;
import net.result.sandnode.exception.DatabaseException;
import net.result.sandnode.mariadb.SandnodeMariaDBDatabase;
import net.result.sandnode.security.PasswordHasher;
import net.result.taulight.db.*;
import org.jetbrains.annotations.NotNull;

import javax.sql.DataSource;
import java.nio.ByteBuffer;
import java.sql.*;
import java.time.ZonedDateTime;
import java.util.*;

public class TauMariaDBDatabase extends SandnodeMariaDBDatabase implements TauDatabase {
    public TauMariaDBDatabase(DataSource dataSource, PasswordHasher hasher) throws DatabaseException {
        super(dataSource, hasher);
    }

    private byte @NotNull [] uuidToBinary(@NotNull UUID uuid) {
        ByteBuffer bb = ByteBuffer.wrap(new byte[16]);
        bb.putLong(uuid.getMostSignificantBits());
        bb.putLong(uuid.getLeastSignificantBits());
        return bb.array();
    }

    private @NotNull UUID binaryToUUID(byte[] bytes) {
        ByteBuffer bb = ByteBuffer.wrap(bytes);
        long firstLong = bb.getLong();
        long secondLong = bb.getLong();
        return new UUID(firstLong, secondLong);
    }

    @Override
    public void initTables(@NotNull Statement stmt) throws SQLException {
        super.initTables(stmt);

        // Base chats table
        stmt.execute("""
            CREATE TABLE IF NOT EXISTS chats (
                chat_id BINARY(16) PRIMARY KEY,
                chat_type ENUM('CHANNEL', 'DIALOG') NOT NULL
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
                server_timestamp TIMESTAMP NOT NULL,
                FOREIGN KEY (chat_id) REFERENCES chats(chat_id) ON DELETE CASCADE,
                FOREIGN KEY (member_id) REFERENCES members(member_id)
            )
        """);

        // Dialogs
        stmt.execute("""
            CREATE TABLE IF NOT EXISTS dialogs (
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
    public TauDialog createDialog(Member member1, Member member2) throws DatabaseException {
        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);
            try {
                TauDialog dialog = new TauDialog(this, member1, member2);

                byte[] chatBin = uuidToBinary(dialog.id());

                try (PreparedStatement stmt = conn.prepareStatement("""
                    INSERT INTO chats (chat_id, chat_type) VALUES (?, 'DIALOG')
                """)) {
                    stmt.setBytes(1, chatBin);
                    stmt.executeUpdate();
                }

                try (PreparedStatement stmt = conn.prepareStatement("""
                    INSERT INTO dialogs (chat_id, member1_id, member2_id) VALUES (?, ?, ?)
                """)) {
                    stmt.setBytes(1, chatBin);
                    stmt.setString(2, member1.id());
                    stmt.setString(3, member2.id());
                    stmt.executeUpdate();
                }

                conn.commit();
                return dialog;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to create dialog", e);
        }
    }

    @Override
    public Optional<TauDialog> findDialog(@NotNull Member m1, @NotNull Member m2) throws DatabaseException {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement("""
                 SELECT chat_id FROM dialogs
                 WHERE (member1_id = ? AND member2_id = ?) OR (member1_id = ? AND member2_id = ?)
             """)) {

            stmt.setString(1, m1.id());
            stmt.setString(2, m2.id());
            stmt.setString(3, m2.id());
            stmt.setString(4, m1.id());

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    byte[] uuidBin = rs.getBytes("chat_id");
                    UUID chatID = binaryToUUID(uuidBin);
                    return Optional.of(new TauDialog(chatID, this, m1, m2));
                }
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new DatabaseException("Failed to find dialog", e);
        }
    }

    @Override
    public void saveChat(@NotNull TauChat chat) throws DatabaseException {
        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);
            try {
                byte[] uuidBin = uuidToBinary(chat.id());

                try (PreparedStatement checkStmt = conn.prepareStatement("""
                    INSERT IGNORE INTO chats (chat_id, chat_type)
                    VALUES (?, ?)
                """)) {
                    checkStmt.setBytes(1, uuidBin);
                    if (chat instanceof TauChannel) {
                        checkStmt.setString(2, "CHANNEL");
                    } else {
                        checkStmt.setString(2, "DIALOG");
                    }
                    checkStmt.executeUpdate();
                }

                if (chat instanceof TauChannel channel) {
                    try (PreparedStatement stmt = conn.prepareStatement("""
                        INSERT INTO channels (chat_id, title, owner_id)
                        VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE title = ?, owner_id = ?
                    """)) {
                        stmt.setBytes(1, uuidBin);
                        stmt.setString(2, channel.title());
                        stmt.setString(3, channel.owner().id());
                        stmt.setString(4, channel.title());
                        stmt.setString(5, channel.owner().id());
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

                try (PreparedStatement typeStmt = conn.prepareStatement("""
                   SELECT chat_type FROM chats
                   WHERE chat_id = ?
                """)) {
                    typeStmt.setBytes(1, chatID);
                    try (ResultSet rs = typeStmt.executeQuery()) {
                        if (rs.next()) {
                            String chatType = rs.getString("chat_type");
                            if ("CHANNEL".equals(chatType)) {
                                return getChannel(conn, id);
                            } else if ("DIALOG".equals(chatType)) {
                                return getDialog(conn, id);
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

    private Optional<TauChat> getChannel(Connection conn, UUID id) throws SQLException, DatabaseException {
        try (PreparedStatement channelStmt = conn.prepareStatement("""
            SELECT title, owner_id FROM channels WHERE chat_id = ?
        """)) {
            byte[] chatID = uuidToBinary(id);
            channelStmt.setBytes(1, chatID);
            try (ResultSet rs = channelStmt.executeQuery()) {
                if (rs.next()) {
                    String title = rs.getString("title");
                    String ownerMemberID = rs.getString("owner_id");
                    Member owner = findMemberByMemberID(ownerMemberID).orElseThrow();
                    return Optional.of(new TauChannel(id, this, title, owner));
                }
            }
        }
        return Optional.empty();
    }

    private Optional<TauChat> getDialog(Connection conn, UUID id) throws SQLException, DatabaseException {
        try (PreparedStatement dialogStmt = conn.prepareStatement("""
            SELECT member1_id, member2_id FROM dialogs WHERE chat_id = ?
        """)) {
            byte[] chatID = uuidToBinary(id);
            dialogStmt.setBytes(1, chatID);
            try (ResultSet rs = dialogStmt.executeQuery()) {
                if (rs.next()) {
                    Member member1 = findMemberByMemberID(rs.getString("member1_id")).orElseThrow();
                    Member member2 = findMemberByMemberID(rs.getString("member2_id")).orElseThrow();
                    return Optional.of(new TauDialog(id, this, member1, member2));
                }
            }
        }
        return Optional.empty();
    }

    @Override
    public void saveMessage(@NotNull ServerChatMessage msg) throws DatabaseException {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement("""
                INSERT INTO messages (message_id, chat_id, content, timestamp, member_id, sys, server_timestamp)
                VALUES (?, ?, ?, ?, ?, ?, ?)
             """)) {

            ChatMessage chatMessage = msg.message();
            stmt.setBytes(2, uuidToBinary(chatMessage.chatID()));
            stmt.setString(3, chatMessage.content());
            stmt.setTimestamp(4, Timestamp.from(chatMessage.ztd().toInstant()));
            stmt.setString(5, chatMessage.memberID());

            stmt.setBytes(1, uuidToBinary(msg.id()));
            stmt.setBoolean(6, msg.sys());
            stmt.setTimestamp(7, Timestamp.from(msg.serverZtd().toInstant()));
            stmt.executeUpdate();
        }
        catch (SQLException | IllegalArgumentException e) {
            throw new DatabaseException("Failed to save message", e);
        }
    }

    @Override
    public List<ServerChatMessage> loadMessages(@NotNull TauChat chat, int index, int size)
            throws DatabaseException {
        try (Connection conn = dataSource.getConnection()) {
            byte[] chatBin = uuidToBinary(chat.id());

            try (PreparedStatement stmt = conn.prepareStatement("""
                SELECT * FROM messages WHERE chat_id = ?
                ORDER BY timestamp DESC LIMIT ? OFFSET ?
             """)) {

                stmt.setBytes(1, chatBin);
                stmt.setInt(2, size);
                stmt.setInt(3, index);

                List<ServerChatMessage> messages = new ArrayList<>();
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        byte[] messageBin = rs.getBytes("message_id");
                        UUID messageID = binaryToUUID(messageBin);

                        ZonedDateTime timestamp = rs.getTimestamp("timestamp")
                                .toInstant()
                                .atZone(ZonedDateTime.now().getZone());

                        ChatMessage message = new ChatMessage()
                                .setChat(chat)
                                .setContent(rs.getString("content"))
                                .setZtd(timestamp)
                                .setMemberID(rs.getString("member_id"));

                        ZonedDateTime serverTimestamp = rs.getTimestamp("server_timestamp")
                                .toInstant()
                                .atZone(ZonedDateTime.now().getZone());

                        ServerChatMessage serverMessage = new ServerChatMessage()
                                .setChatMessage(message)
                                .setID(messageID)
                                .setServerZtd(serverTimestamp)
                                .setSys(rs.getBoolean("sys"));

                        messages.add(serverMessage);
                    }
                }
                return messages;
            }
        } catch (SQLException | IllegalArgumentException e) {
            throw new DatabaseException("Failed to load messages", e);
        }
    }

    @Override
    public Collection<Member> getMembersFromChat(@NotNull TauChat chat) throws DatabaseException {
        try (Connection conn = dataSource.getConnection()) {
            byte[] chatID = uuidToBinary(chat.id());

            try (PreparedStatement typeStmt = conn.prepareStatement("""
                SELECT chat_type FROM chats
                WHERE chat_id = ?
            """)) {
                typeStmt.setBytes(1, chatID);

                try (ResultSet typeRs = typeStmt.executeQuery()) {
                    if (typeRs.next() && "DIALOG".equals(typeRs.getString("chat_type"))) {
                        return getDialogMembers(conn, chatID);
                    }
                }
            }

            return getChannelMembers(conn, chatID);
        } catch (SQLException | IllegalArgumentException e) {
            throw new DatabaseException("Failed to get chat members", e);
        }
    }

    private @NotNull List<Member> getDialogMembers(Connection conn, byte[] chatID)
            throws SQLException, DatabaseException {
        try (PreparedStatement stmt = conn.prepareStatement("""
                SELECT member1_id, member2_id FROM dialogs
                WHERE chat_id = ?
            """)) {
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

    private @NotNull List<Member> getChannelMembers(Connection conn, byte[] chatID) throws SQLException {
        try (PreparedStatement stmt = conn.prepareStatement("""
            SELECT m.member_id, m.password_hash FROM members m
            JOIN chat_members cm ON m.member_id = cm.member_id WHERE cm.chat_id = ?
        """)) {
            stmt.setBytes(1, chatID);
            List<Member> members = new ArrayList<>();
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    members.add(new StandardMember(
                        rs.getString("member_id"),
                        rs.getString("password_hash")
                    ));
                }
            }
            return members;
        }
    }

    @Override
    public void addMemberToChat(@NotNull TauChat chat, @NotNull Member member) throws DatabaseException {
        try (Connection conn = dataSource.getConnection()) {
            byte[] chatID = uuidToBinary(chat.id());

            try (PreparedStatement stmt = conn.prepareStatement("""
              INSERT INTO chat_members (chat_id, member_id)
              VALUES (?, ?)
            """)) {
                stmt.setBytes(1, chatID);
                stmt.setString(2, member.id());
                stmt.executeUpdate();
            }
        } catch (SQLException | IllegalArgumentException e) {
            throw new DatabaseException("Failed to add member to chat", e);
        }
    }

    @Override
    public void leaveFromChat(@NotNull TauChannel channel, @NotNull Member member) throws DatabaseException {
        try (Connection conn = dataSource.getConnection()) {
            byte[] chatID = uuidToBinary(channel.id());

            conn.setAutoCommit(false);
            try {
                try (PreparedStatement stmt = conn.prepareStatement("""
                    DELETE FROM chat_members
                    WHERE chat_id = ? AND member_id = ?
                """)) {
                    stmt.setBytes(1, chatID);
                    stmt.setString(2, member.id());
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
    public Collection<TauChat> getChats(@NotNull Member member) throws DatabaseException {
        try (Connection conn = dataSource.getConnection()) {
            List<TauChat> chats = new ArrayList<>();

            // Get channels
            try (PreparedStatement stmt = conn.prepareStatement("""
                    SELECT c.chat_id, ch.title, ch.owner_id FROM chats c
                    JOIN chat_members cm ON c.chat_id = cm.chat_id
                    JOIN channels ch ON c.chat_id = ch.chat_id
                    WHERE cm.member_id = ? AND c.chat_type = 'CHANNEL'
                """)) {

                stmt.setString(1, member.id());
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        byte[] chatBin = rs.getBytes("chat_id");
                        UUID chatID = binaryToUUID(chatBin);
                        String title = rs.getString("title");
                        String ownerMemberID = rs.getString("owner_id");
                        Member owner = findMemberByMemberID(ownerMemberID).orElseThrow();
                        chats.add(new TauChannel(chatID, this, title, owner));
                    }
                }
            }

            // Get dialogs
            try (PreparedStatement stmt = conn.prepareStatement("""
                SELECT c.chat_id,
                       CASE WHEN d.member1_id = ?
                            THEN d.member2_id
                            ELSE d.member1_id
                       END AS other_member_id
                FROM chats c
                JOIN dialogs d
                ON c.chat_id = d.chat_id
                WHERE c.chat_type = 'DIALOG'
                AND (d.member1_id = ? OR d.member2_id = ?)
            """)) {
                stmt.setString(1, member.id());
                stmt.setString(2, member.id());
                stmt.setString(3, member.id());

                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        byte[] chatBin = rs.getBytes("chat_id");
                        UUID chatID = binaryToUUID(chatBin);
                        String otherMemberID = rs.getString("other_member_id");
                        Member otherMember = findMemberByMemberID(otherMemberID).orElseThrow();
                        chats.add(new TauDialog(chatID, this, member, otherMember));
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

            try (PreparedStatement stmt = conn.prepareStatement("""
                DELETE FROM chats
                WHERE chat_id = ?
            """)) {
                stmt.setBytes(1, chatBin);
                stmt.executeUpdate();
            }
        } catch (SQLException | IllegalArgumentException e) {
            throw new DatabaseException("Failed to remove chat", e);
        }
    }

    @Override
    public long getMessageCount(@NotNull TauChat chat) throws DatabaseException {
        try (Connection conn = dataSource.getConnection()) {
            byte[] chatBin = uuidToBinary(chat.id());

            try (PreparedStatement stmt = conn.prepareStatement("""
                SELECT COUNT(*) FROM messages
                WHERE chat_id = ?
            """)) {
                stmt.setBytes(1, chatBin);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) return rs.getLong(1);
                }
            }
            return 0;
        } catch (SQLException | IllegalArgumentException e) {
            throw new DatabaseException("Failed to get message count", e);
        }
    }
}