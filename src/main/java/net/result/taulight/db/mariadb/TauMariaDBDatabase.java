package net.result.taulight.db.mariadb;

import net.result.sandnode.config.MariaDBConfig;
import net.result.sandnode.db.Member;
import net.result.sandnode.exception.DatabaseException;
import net.result.sandnode.db.mariadb.SandnodeMariaDBDatabase;
import net.result.sandnode.security.PasswordHasher;
import net.result.sandnode.util.UUIDUtil;
import net.result.taulight.dto.ChatMessageInputDTO;
import net.result.taulight.dto.ChatMessageViewDTO;
import net.result.taulight.db.*;
import net.result.taulight.exception.AlreadyExistingRecordException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.*;
import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

public class TauMariaDBDatabase extends SandnodeMariaDBDatabase implements TauDatabase {
    public TauMariaDBDatabase(MariaDBConfig mariaDBConfig, PasswordHasher hasher) throws DatabaseException {
        super(mariaDBConfig, hasher);
    }

    @Override
    public void initTables(@NotNull Statement stmt) throws SQLException {
        super.initTables(stmt);

        // Base chats table
        stmt.execute("""
            CREATE TABLE IF NOT EXISTS chats (
                chat_id BINARY(16) PRIMARY KEY,
                chat_type ENUM('CHANNEL', 'DIALOG') NOT NULL,
                created_at TIMESTAMP NOT NULL
            )
        """);

        // Channels table extending chats
        stmt.execute("""
            CREATE TABLE IF NOT EXISTS channels (
                chat_id BINARY(16) PRIMARY KEY,
                title VARCHAR(255) NOT NULL,
                owner_id VARCHAR(255) NOT NULL,
                FOREIGN KEY (chat_id) REFERENCES chats(chat_id) ON DELETE CASCADE,
                FOREIGN KEY (owner_id) REFERENCES members(nickname)
            )
        """);

        // Chat members (many-to-many relationship)
        stmt.execute("""
            CREATE TABLE IF NOT EXISTS chat_members (
                chat_id BINARY(16),
                nickname VARCHAR(255),
                PRIMARY KEY (chat_id, nickname),
                FOREIGN KEY (chat_id) REFERENCES chats(chat_id) ON DELETE CASCADE,
                FOREIGN KEY (nickname) REFERENCES members(nickname)
            )
        """);

        // Messages table
        stmt.execute("""
            CREATE TABLE IF NOT EXISTS messages (
                message_id BINARY(16) PRIMARY KEY,
                chat_id BINARY(16),
                content TEXT NOT NULL,
                timestamp TIMESTAMP NOT NULL,
                nickname VARCHAR(255),
                sys BOOLEAN,
                created_at TIMESTAMP NOT NULL,
                FOREIGN KEY (chat_id) REFERENCES chats(chat_id) ON DELETE CASCADE,
                FOREIGN KEY (nickname) REFERENCES members(nickname)
            )
        """);

        // Message replies table
        stmt.execute("""
            CREATE TABLE IF NOT EXISTS message_replies (
                message_id BINARY(16),
                reply_to_id BINARY(16),
                PRIMARY KEY (message_id, reply_to_id),
                FOREIGN KEY (message_id) REFERENCES messages(message_id) ON DELETE CASCADE,
                FOREIGN KEY (reply_to_id) REFERENCES messages(message_id) ON DELETE CASCADE
            )
        """);

        stmt.execute("""
            CREATE TABLE IF NOT EXISTS reaction_types (
                reaction_type_id BINARY(16) PRIMARY KEY,
                package_name VARCHAR(255) NOT NULL,
                name VARCHAR(255) NOT NULL UNIQUE,
                created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
            )
        """);

        stmt.execute("""
            CREATE TABLE IF NOT EXISTS reaction_entries (
                reaction_entry_id BINARY(16) PRIMARY KEY,
                message_id BINARY(16) NOT NULL,
                reaction_type_id BINARY(16) NOT NULL,
                nickname VARCHAR(255) NOT NULL,
                created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (message_id) REFERENCES messages(message_id) ON DELETE CASCADE,
                FOREIGN KEY (reaction_type_id) REFERENCES reaction_types(reaction_type_id) ON DELETE CASCADE,
                FOREIGN KEY (nickname) REFERENCES members(nickname) ON DELETE CASCADE
            )
        """);

        // Dialogs
        stmt.execute("""
            CREATE TABLE IF NOT EXISTS dialogs (
                chat_id BINARY(16) PRIMARY KEY,
                member1_id VARCHAR(255) NOT NULL,
                member2_id VARCHAR(255) NOT NULL,
                FOREIGN KEY (chat_id) REFERENCES chats(chat_id) ON DELETE CASCADE,
                FOREIGN KEY (member1_id) REFERENCES members(nickname),
                FOREIGN KEY (member2_id) REFERENCES members(nickname),
                UNIQUE KEY (member1_id, member2_id)
            )
        """);

        // Invite tokens
        stmt.execute("""
            CREATE TABLE IF NOT EXISTS invite_codes (
                token_id BINARY(16) PRIMARY KEY,
                expires_at TIMESTAMP NOT NULL,
                nickname VARCHAR(255) NOT NULL,
                chat_id BINARY(16) NOT NULL,
                code VARCHAR(255) NOT NULL UNIQUE,
                created_at TIMESTAMP NOT NULL,
                sender_nickname VARCHAR(255) NOT NULL,
                activated_at TIMESTAMP NULL,
                FOREIGN KEY (chat_id) REFERENCES chats(chat_id) ON DELETE CASCADE,
                FOREIGN KEY (nickname) REFERENCES members(nickname),
                FOREIGN KEY (sender_nickname) REFERENCES members(nickname)
            )
        """);

    }

    @Override
    public TauDialog createDialog(Member member1, Member member2) throws DatabaseException {
        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);
            try {
                TauDialog dialog = new TauDialog(this, member1, member2);

                byte[] chatBin = UUIDUtil.uuidToBinary(dialog.id());

                while (true) {
                    try (PreparedStatement stmt = conn.prepareStatement("""
                        INSERT INTO chats (chat_id, chat_type, created_at) VALUES (?, 'DIALOG', ?)
                    """)) {
                        stmt.setBytes(1, chatBin);
                        stmt.setTimestamp(2, Timestamp.from(dialog.getCreationDate().toInstant()));
                        stmt.executeUpdate();
                        break;
                    } catch (SQLIntegrityConstraintViolationException e) {
                        dialog.setRandomID();
                    }
                }

                try (PreparedStatement stmt = conn.prepareStatement("""
                    INSERT INTO dialogs (chat_id, member1_id, member2_id) VALUES (?, ?, ?)
                """)) {
                    stmt.setBytes(1, chatBin);
                    stmt.setString(2, member1.nickname());
                    stmt.setString(3, member2.nickname());
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

            stmt.setString(1, m1.nickname());
            stmt.setString(2, m2.nickname());
            stmt.setString(3, m2.nickname());
            stmt.setString(4, m1.nickname());

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    byte[] uuidBin = rs.getBytes("chat_id");

                    ZonedDateTime createdAt = rs
                            .getTimestamp("created_at").toInstant()
                            .atZone(ZonedDateTime.now().getZone());

                    UUID chatID = UUIDUtil.binaryToUUID(uuidBin);
                    return Optional.of(new TauDialog(chatID, createdAt, this, m1, m2));
                }
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new DatabaseException("Failed to find dialog", e);
        }
    }

    @Override
    public void saveChat(@NotNull TauChat chat) throws DatabaseException, AlreadyExistingRecordException {
        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);
            try {
                byte[] uuidBin = UUIDUtil.uuidToBinary(chat.id());

                try (PreparedStatement checkStmt = conn.prepareStatement("""
                    INSERT IGNORE INTO chats (chat_id, chat_type, created_at)
                    VALUES (?, ?, ?)
                """)) {
                    checkStmt.setBytes(1, uuidBin);
                    if (chat instanceof TauChannel) {
                        checkStmt.setString(2, "CHANNEL");
                    } else {
                        checkStmt.setString(2, "DIALOG");
                    }
                    checkStmt.setTimestamp(3, Timestamp.from(chat.getCreationDate().toInstant()));
                    checkStmt.executeUpdate();
                } catch (SQLIntegrityConstraintViolationException e) {
                    throw new AlreadyExistingRecordException("Chat", "chat_id", chat.id(), e);
                }

                if (chat instanceof TauChannel channel) {
                    try (PreparedStatement stmt = conn.prepareStatement("""
                        INSERT INTO channels (chat_id, title, owner_id)
                        VALUES (?, ?, ?)
                    """)) {
                        stmt.setBytes(1, uuidBin);
                        stmt.setString(2, channel.title());
                        stmt.setString(3, channel.owner().nickname());
                        stmt.executeUpdate();
                    } catch (SQLIntegrityConstraintViolationException e) {
                        throw new AlreadyExistingRecordException("Channel", "chat_id", chat.id(), e);
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
                byte[] chatID = UUIDUtil.uuidToBinary(id);

                try (PreparedStatement typeStmt = conn.prepareStatement("""
                   SELECT chat_type, created_at FROM chats
                   WHERE chat_id = ?
                """)) {
                    typeStmt.setBytes(1, chatID);
                    try (ResultSet rs = typeStmt.executeQuery()) {
                        if (rs.next()) {
                            String chatType = rs.getString("chat_type");
                            ZonedDateTime createdAt = rs
                                    .getTimestamp("created_at").toInstant()
                                    .atZone(ZonedDateTime.now().getZone());
                            if (chatType.equals("CHANNEL")) {
                                return getChannel(conn, createdAt, id);
                            } else if (chatType.equals("DIALOG")) {
                                return getDialog(conn, createdAt, id);
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

    private Optional<TauChat> getChannel(Connection conn, ZonedDateTime createdAt, UUID id)
            throws SQLException, DatabaseException {
        try (PreparedStatement channelStmt = conn.prepareStatement("""
            SELECT title, owner_id FROM channels WHERE chat_id = ?
        """)) {
            byte[] chatID = UUIDUtil.uuidToBinary(id);
            channelStmt.setBytes(1, chatID);
            try (ResultSet rs = channelStmt.executeQuery()) {
                if (rs.next()) {
                    String title = rs.getString("title");
                    String ownerNickname = rs.getString("owner_id");

                    Member owner = findMemberByNickname(ownerNickname).orElseThrow();
                    return Optional.of(new TauChannel(id, createdAt, this, title, owner));
                }
            }
        }
        return Optional.empty();
    }

    private Optional<TauChat> getDialog(Connection conn, ZonedDateTime createdAt, UUID id)
            throws SQLException, DatabaseException {
        try (PreparedStatement dialogStmt = conn.prepareStatement("""
            SELECT member1_id, member2_id FROM dialogs WHERE chat_id = ?
        """)) {
            byte[] chatID = UUIDUtil.uuidToBinary(id);
            dialogStmt.setBytes(1, chatID);
            try (ResultSet rs = dialogStmt.executeQuery()) {
                if (rs.next()) {
                    Member member1 = findMemberByNickname(rs.getString("member1_id")).orElseThrow();
                    Member member2 = findMemberByNickname(rs.getString("member2_id")).orElseThrow();

                    return Optional.of(new TauDialog(id, createdAt, this, member1, member2));
                }
            }
        }
        return Optional.empty();
    }

    @Override
    public void saveMessage(@NotNull ChatMessageViewDTO msg) throws DatabaseException, AlreadyExistingRecordException {
        ChatMessageInputDTO input = msg.message();

        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);
            try {
                // Existing message saving code
                try (PreparedStatement stmt = connection.prepareStatement("""
                 INSERT INTO messages
                 (message_id, created_at, chat_id, content, timestamp, nickname, sys)
                 VALUES (?, ?, ?, ?, ?, ?, ?)
             """)) {
                    stmt.setBytes(1, UUIDUtil.uuidToBinary(msg.id()));
                    stmt.setTimestamp(2, Timestamp.from(msg.getCreationDate().toInstant()));
                    stmt.setBytes(3, UUIDUtil.uuidToBinary(input.chatID()));
                    stmt.setString(4, input.content());
                    stmt.setTimestamp(5, Timestamp.from(input.ztd().toInstant()));
                    stmt.setString(6, input.nickname());
                    stmt.setBoolean(7, input.sys());
                    stmt.executeUpdate();
                }

                // Save replies if any exist
                List<UUID> replies = input.replies();
                if (replies != null && !replies.isEmpty()) {
                    try (PreparedStatement replyStmt = connection.prepareStatement("""
                        INSERT INTO message_replies (message_id, reply_to_id) VALUES (?, ?)
                    """)) {
                        byte[] messageIdBin = UUIDUtil.uuidToBinary(msg.id());
                        replyStmt.setBytes(1, messageIdBin);

                        for (UUID replyToId : replies) {
                            replyStmt.setBytes(2, UUIDUtil.uuidToBinary(replyToId));
                            replyStmt.addBatch();
                        }
                        replyStmt.executeBatch();
                    }
                }

                connection.commit();
            } catch (SQLIntegrityConstraintViolationException e) {
                connection.rollback();
                String errorMessage = e.getMessage();

                if (errorMessage.contains("message_id")) {
                    throw new AlreadyExistingRecordException("Messages Table", "message_id", msg.id(), e);
                } else if (errorMessage.contains("nickname")) {
                    throw new AlreadyExistingRecordException("Messages Table", "nickname", input.nickname(), e);
                } else if (errorMessage.contains("reply_to_id")) {
                    throw new AlreadyExistingRecordException(
                            "Message Replies Table", "reply_to_id", "one of the reply IDs", e);
                } else {
                    throw new AlreadyExistingRecordException("Messages Table", "chat_id (?)", input.chatID(), e);
                }
            } catch (SQLException e) {
                connection.rollback();
                throw new DatabaseException("Failed to save message", e);
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLException | IllegalArgumentException e) {
            throw new DatabaseException("Failed to save message", e);
        }
    }

    @Override
    public List<ChatMessageViewDTO> loadMessages(@NotNull TauChat chat, int index, int size) throws DatabaseException {
        try (Connection conn = dataSource.getConnection()) {
            byte[] chatBin = UUIDUtil.uuidToBinary(chat.id());

            List<ChatMessageViewDTO> messages = new ArrayList<>();
            Map<UUID, ChatMessageViewDTO> messageMap = new HashMap<>();

            try (PreparedStatement stmt = conn.prepareStatement("""
                 SELECT * FROM messages WHERE chat_id = ?
                 ORDER BY timestamp DESC, message_id DESC LIMIT ? OFFSET ?
            """)) {
                stmt.setBytes(1, chatBin);
                stmt.setInt(2, size);
                stmt.setInt(3, index);

                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        UUID messageID = UUIDUtil.binaryToUUID(rs.getBytes("message_id"));
                        ZonedDateTime timestamp = rs.getTimestamp("timestamp")
                                .toInstant()
                                .atZone(ZoneId.of("UTC"));
                        ZonedDateTime createdAt = rs.getTimestamp("created_at")
                                .toInstant()
                                .atZone(ZoneId.of("UTC"));

                        ChatMessageInputDTO message = new ChatMessageInputDTO()
                                .setChat(chat)
                                .setContent(rs.getString("content"))
                                .setZtd(timestamp)
                                .setNickname(rs.getString("nickname"))
                                .setSys(rs.getBoolean("sys"));

                        ChatMessageViewDTO serverMessage = new ChatMessageViewDTO();
                        serverMessage.setID(messageID);
                        serverMessage.setChatMessageInputDTO(message);
                        serverMessage.setCreationDate(createdAt);

                        messages.add(serverMessage);
                        messageMap.put(messageID, serverMessage);
                    }
                }
            }

            if (messages.isEmpty()) return messages;

            String placeholders = messages.stream().map(m -> "?").collect(Collectors.joining(","));
            try (PreparedStatement replyStmt = conn.prepareStatement("""
                SELECT message_id, reply_to_id FROM message_replies
                WHERE message_id IN (%s)
            """.formatted(placeholders))) {
                for (int i = 0; i < messages.size(); i++) {
                    replyStmt.setBytes(i + 1, UUIDUtil.uuidToBinary(messages.get(i).id()));
                }

                try (ResultSet rs = replyStmt.executeQuery()) {
                    while (rs.next()) {
                        UUID msgId = UUIDUtil.binaryToUUID(rs.getBytes("message_id"));
                        UUID replyToId = UUIDUtil.binaryToUUID(rs.getBytes("reply_to_id"));

                        ChatMessageViewDTO serverMsg = messageMap.get(msgId);
                        if (serverMsg != null) {
                            serverMsg.message().addReply(replyToId);
                        }
                    }
                }
            }

            try (PreparedStatement reactionStmt = conn.prepareStatement("""
                SELECT r.*, t.name, t.package_name, t.created_at AS type_created
                FROM reaction_entries r
                JOIN reaction_types t ON r.reaction_type_id = t.reaction_type_id
                WHERE r.message_id IN (%s)
            """.formatted(placeholders))) {
                for (int i = 0; i < messages.size(); i++) {
                    reactionStmt.setBytes(i + 1, UUIDUtil.uuidToBinary(messages.get(i).id()));
                }

                try (ResultSet rs = reactionStmt.executeQuery()) {
                    while (rs.next()) {
                        UUID msgId = UUIDUtil.binaryToUUID(rs.getBytes("message_id"));
                        ChatMessageViewDTO serverMsg = messageMap.get(msgId);
                        if (serverMsg == null) continue;

                        String reaction = "%s:%s".formatted(rs.getString("package_name"), rs.getString("name"));
                        serverMsg.addReaction(reaction, rs.getString("nickname"));
                    }
                }
            }

            return messages;

        } catch (SQLException | IllegalArgumentException e) {
            throw new DatabaseException("Failed to load messages", e);
        }
    }

    @Override
    public Optional<ChatMessageViewDTO> findMessage(UUID id) throws DatabaseException {
        try (Connection conn = dataSource.getConnection()) {
            try (PreparedStatement stmt = conn.prepareStatement("""
                SELECT * FROM messages
                WHERE message_id = ?
            """)) {
                stmt.setBytes(1, UUIDUtil.uuidToBinary(id));

                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        UUID chatId = UUIDUtil.binaryToUUID(rs.getBytes("chat_id"));
                        TauChat chat = getChat(chatId).orElse(null);
                        if (chat == null) return Optional.empty();

                        ChatMessageInputDTO input = new ChatMessageInputDTO()
                                .setChat(chat)
                                .setContent(rs.getString("content"))
                                .setZtd(rs.getTimestamp("timestamp").toInstant().atZone(ZoneId.of("UTC")))
                                .setNickname(rs.getString("nickname"))
                                .setSys(rs.getBoolean("sys"));

                        ChatMessageViewDTO serverMsg = new ChatMessageViewDTO();
                        serverMsg.setID(id);
                        serverMsg.setChatMessageInputDTO(input);
                        serverMsg.setCreationDate(rs.getTimestamp("created_at")
                                .toInstant()
                                .atZone(ZoneId.of("UTC")));

                        try (PreparedStatement replyStmt = conn.prepareStatement("""
                            SELECT reply_to_id FROM message_replies WHERE message_id = ?
                        """)) {
                            replyStmt.setBytes(1, UUIDUtil.uuidToBinary(id));
                            try (ResultSet rs2 = replyStmt.executeQuery()) {
                                while (rs2.next()) {
                                    UUID replyToId = UUIDUtil.binaryToUUID(rs2.getBytes("reply_to_id"));
                                    input.addReply(replyToId);
                                }
                            }
                        }

                        try (PreparedStatement reactionStmt = conn.prepareStatement("""
                            SELECT r.*, t.name, t.package_name, t.created_at AS type_created
                            FROM reaction_entries r
                            JOIN reaction_types t ON r.reaction_type_id = t.reaction_type_id
                            WHERE r.message_id = ?
                        """)) {
                            reactionStmt.setBytes(1, UUIDUtil.uuidToBinary(id));
                            try (ResultSet rs3 = reactionStmt.executeQuery()) {
                                while (rs3.next()) {
                                    String packageName = rs3.getString("package_name");
                                    String name = rs3.getString("name");
                                    String reaction = "%s:%s".formatted(packageName, name);
                                    serverMsg.addReaction(reaction, rs3.getString("nickname"));
                                }
                            }
                        }

                        return Optional.of(serverMsg);
                    }
                }
            }
            return Optional.empty();
        } catch (SQLException | IllegalArgumentException e) {
            throw new DatabaseException("Failed to find message", e);
        }
    }

    @Override
    public Collection<Member> getMembersFromChannel(@NotNull TauChannel channel) throws DatabaseException {
        byte[] chatID = UUIDUtil.uuidToBinary(channel.id());
        try (
                Connection conn = dataSource.getConnection();
                PreparedStatement stmt = conn.prepareStatement("""
                    SELECT m.member_id, m.created_at, m.nickname, m.password_hash
                    FROM members m
                    JOIN chat_members cm ON m.nickname = cm.nickname
                    WHERE cm.chat_id = ?
                """)
        ) {
            stmt.setBytes(1, chatID);
            Collection<Member> members = new ArrayList<>();
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    byte[] uuidBin = rs.getBytes("member_id");

                    ZonedDateTime createdAt = rs
                            .getTimestamp("created_at").toInstant()
                            .atZone(ZonedDateTime.now().getZone());

                    UUID id = UUIDUtil.binaryToUUID(uuidBin);

                    String nickname = rs.getString("nickname");
                    String passwordHash = rs.getString("password_hash");

                    members.add(new Member(this, id, createdAt, nickname, passwordHash));
                }
            }
            return members;
        } catch (SQLException | IllegalArgumentException e) {
            throw new DatabaseException("Failed to get chat members", e);
        }
    }

    @Override
    public void addMemberToChat(@NotNull TauChat chat, @NotNull Member member) throws DatabaseException {
        try (Connection conn = dataSource.getConnection()) {
            byte[] chatID = UUIDUtil.uuidToBinary(chat.id());

            try (PreparedStatement stmt = conn.prepareStatement("""
                INSERT INTO chat_members (chat_id, nickname) VALUES (?, ?)
            """)) {
                stmt.setBytes(1, chatID);
                stmt.setString(2, member.nickname());
                stmt.executeUpdate();
            }
        } catch (SQLException | IllegalArgumentException e) {
            throw new DatabaseException("Failed to add member to chat", e);
        }
    }

    @Override
    public void leaveFromChat(@NotNull TauChat chat, @NotNull Member member) throws DatabaseException {
        try (Connection conn = dataSource.getConnection()) {
            byte[] chatID = UUIDUtil.uuidToBinary(chat.id());

            conn.setAutoCommit(false);
            try {
                try (PreparedStatement stmt = conn.prepareStatement("""
                    DELETE FROM chat_members
                    WHERE chat_id = ? AND nickname = ?
                """)) {
                    stmt.setBytes(1, chatID);
                    stmt.setString(2, member.nickname());
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
                    SELECT c.chat_id, ch.title, ch.owner_id, c.created_at FROM chats c
                    JOIN chat_members cm ON c.chat_id = cm.chat_id
                    JOIN channels ch ON c.chat_id = ch.chat_id
                    WHERE cm.nickname = ? AND c.chat_type = 'CHANNEL'
                """)) {

                stmt.setString(1, member.nickname());
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        byte[] chatBin = rs.getBytes("chat_id");
                        String title = rs.getString("title");
                        String otherNickname = rs.getString("owner_id");
                        ZonedDateTime createdAt = rs
                                .getTimestamp("created_at").toInstant()
                                .atZone(ZonedDateTime.now().getZone());
                        UUID chatID = UUIDUtil.binaryToUUID(chatBin);
                        Member owner = findMemberByNickname(otherNickname).orElseThrow();
                        chats.add(new TauChannel(chatID, createdAt, this, title, owner));
                    }
                }
            }

            // Get dialogs
            try (PreparedStatement stmt = conn.prepareStatement("""
                SELECT c.chat_id,
                       CASE WHEN d.member1_id = ?
                            THEN d.member2_id
                            ELSE d.member1_id
                       END AS other_nickname,
                       c.created_at
                FROM chats c
                JOIN dialogs d ON c.chat_id = d.chat_id
                WHERE c.chat_type = 'DIALOG' AND (d.member1_id = ? OR d.member2_id = ?)
            """)) {
                stmt.setString(1, member.nickname());
                stmt.setString(2, member.nickname());
                stmt.setString(3, member.nickname());

                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        byte[] chatBin = rs.getBytes("chat_id");
                        String otherNickname = rs.getString("other_nickname");
                        ZonedDateTime createdAt = rs
                                .getTimestamp("created_at").toInstant()
                                .atZone(ZonedDateTime.now().getZone());
                        UUID chatID = UUIDUtil.binaryToUUID(chatBin);
                        Member otherMember = findMemberByNickname(otherNickname).orElseThrow();
                        chats.add(new TauDialog(chatID, createdAt, this, member, otherMember));
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
            byte[] chatBin = UUIDUtil.uuidToBinary(chatID);

            try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM chats WHERE chat_id = ?")) {
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
            byte[] chatBin = UUIDUtil.uuidToBinary(chat.id());

            try (PreparedStatement stmt = conn.prepareStatement("SELECT COUNT(*) FROM messages WHERE chat_id = ?")) {
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

    @Override
    public void createInviteCode(@NotNull InviteCodeObject code)
            throws DatabaseException, AlreadyExistingRecordException {
        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);
            try {
                byte[] tokenId = UUIDUtil.uuidToBinary(code.id());
                byte[] chatId = UUIDUtil.uuidToBinary(code.getChatID());

                try (PreparedStatement stmt = conn.prepareStatement("""
                    INSERT INTO invite_codes
                    (token_id, expires_at, nickname, chat_id, code, created_at, sender_nickname, activated_at)
                    VALUES (?, ?, ?, ?, ?, ?, ?, NULL)
                """)) {
                    stmt.setBytes(1, tokenId);
                    stmt.setTimestamp(2, Timestamp.from(code.getExpiresData().toInstant()));
                    stmt.setString(3, code.getNickname());
                    stmt.setBytes(4, chatId);
                    stmt.setString(5, code.getCode());
                    stmt.setTimestamp(6, Timestamp.from(code.getCreationDate().toInstant()));
                    stmt.setString(7, code.getSenderNickname());

                    stmt.executeUpdate();
                } catch (SQLIntegrityConstraintViolationException e) {
                    String errorMessage = e.getMessage();

                    if (errorMessage.contains("token_id")) {
                        throw new AlreadyExistingRecordException("Invite Tokens", "token_id", code.id(), e);
                    } else if (errorMessage.contains("code")) {
                        throw new AlreadyExistingRecordException(
                                "Invite Tokens", "code", code.getCode(), e);
                    } else if (errorMessage.contains("nickname")) {
                        throw new AlreadyExistingRecordException(
                                "Invite Tokens", "nickname", code.getNickname(), e);
                    } else if (errorMessage.contains("chat_id")) {
                        throw new AlreadyExistingRecordException(
                                "Invite Tokens", "chat_id", code.getChatID(), e);
                    } else if (errorMessage.contains("sender_nickname")) {
                        throw new AlreadyExistingRecordException(
                                "Invite Tokens", "sender_nickname", code.getSenderNickname(), e);
                    } else {
                        throw new AlreadyExistingRecordException("Invite Tokens", e);
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
            throw new DatabaseException("Failed to create invite link", e);
        }
    }

    @Override
    public Optional<InviteCodeObject> getInviteCode(String code) throws DatabaseException {
        try (
                Connection conn = dataSource.getConnection();
                PreparedStatement stmt = conn.prepareStatement("""
                    SELECT token_id, expires_at, nickname, chat_id, created_at, sender_nickname, activated_at
                    FROM invite_codes
                    WHERE code = ?
                """)
        ) {

            stmt.setString(1, code);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    byte[] tokenIdBin = rs.getBytes("token_id");
                    UUID tokenId = UUIDUtil.binaryToUUID(tokenIdBin);

                    ZonedDateTime expiresAt = rs
                            .getTimestamp("expires_at").toInstant()
                            .atZone(ZonedDateTime.now().getZone());

                    String nickname = rs.getString("nickname");

                    byte[] chatIdBin = rs.getBytes("chat_id");
                    UUID chatId = UUIDUtil.binaryToUUID(chatIdBin);

                    ZonedDateTime createdAt = rs
                            .getTimestamp("created_at").toInstant()
                            .atZone(ZonedDateTime.now().getZone());

                    String senderId = rs.getString("sender_nickname");

                    Timestamp activatedTimestamp = rs.getTimestamp("activated_at");
                    ZonedDateTime activatedAt = activatedTimestamp != null ?
                            activatedTimestamp.toInstant().atZone(ZonedDateTime.now().getZone()) : null;

                    return Optional.of(new InviteCodeObject(
                            this, tokenId, createdAt, code, chatId, nickname, senderId, expiresAt, activatedAt
                    ));
                }
                return Optional.empty();
            }
        } catch (SQLException | IllegalArgumentException e) {
            throw new DatabaseException("Failed to retrieve invite link", e);
        }
    }

    @Override
    public boolean activateInviteCode(@NotNull InviteCodeObject code) throws DatabaseException {
        ZonedDateTime activationTime = ZonedDateTime.now(ZoneOffset.UTC); // Ensure UTC consistency

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement("""
                 UPDATE invite_codes
                 SET activated_at = ?
                 WHERE code = ? AND activated_at IS NULL
             """)) {

            stmt.setTimestamp(1, Timestamp.from(activationTime.toInstant())); // Set precise activation time
            stmt.setString(2, code.getCode());

            int updated = stmt.executeUpdate();
            if (updated > 0) {
                code.setActivationDate(activationTime); // Update the object's field
                return true;
            }
            return false;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to activate invite link", e);
        }
    }

    @Override
    public boolean deleteInviteCode(String code) throws DatabaseException {
        try (
                Connection conn = dataSource.getConnection();
                PreparedStatement stmt = conn.prepareStatement("DELETE FROM invite_codes WHERE code = ?")
        ) {
            stmt.setString(1, code);
            int affected = stmt.executeUpdate();
            return affected > 0;
        } catch (SQLException e) {
            throw new DatabaseException("Failed to delete invite link", e);
        }
    }

    @Override
    public List<InviteCodeObject> getInviteCodesBySender(
            String senderNickname,
            boolean includeExpired,
            boolean includeActivated
    ) throws DatabaseException {
        try (Connection conn = dataSource.getConnection()) {
            StringBuilder queryBuilder = new StringBuilder("""
                SELECT token_id, expires_at, nickname, chat_id, code, created_at, activated_at
                FROM invite_codes
                WHERE sender_nickname = ?
            """);

            if (!includeExpired) {
                queryBuilder.append(" AND expires_at > NOW()");
            }

            if (!includeActivated) {
                queryBuilder.append(" AND activated_at IS NULL");
            }

            try (PreparedStatement stmt = conn.prepareStatement(queryBuilder.toString())) {
                stmt.setString(1, senderNickname);

                List<InviteCodeObject> tokens = new ArrayList<>();
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        byte[] tokenIdBin = rs.getBytes("token_id");
                        UUID tokenId = UUIDUtil.binaryToUUID(tokenIdBin);

                        ZonedDateTime expiresAt = rs
                                .getTimestamp("expires_at").toInstant()
                                .atZone(ZonedDateTime.now().getZone());

                        String nickname = rs.getString("nickname");

                        byte[] chatIdBin = rs.getBytes("chat_id");
                        UUID chatId = UUIDUtil.binaryToUUID(chatIdBin);

                        String code = rs.getString("code");

                        ZonedDateTime createdAt = rs
                                .getTimestamp("created_at").toInstant()
                                .atZone(ZonedDateTime.now().getZone());

                        Timestamp activatedTimestamp = rs.getTimestamp("activated_at");
                        ZonedDateTime activatedAt = activatedTimestamp != null ?
                                activatedTimestamp.toInstant().atZone(ZonedDateTime.now().getZone()) : null;

                        InviteCodeObject token = new InviteCodeObject(this, tokenId, createdAt,
                                code, chatId, nickname, senderNickname, expiresAt, activatedAt);
                        tokens.add(token);
                    }
                }
                return tokens;
            }
        } catch (SQLException | IllegalArgumentException e) {
            throw new DatabaseException("Failed to retrieve invite links by sender", e);
        }
    }


    @Override
    public List<InviteCodeObject> getActiveInviteCodes(@NotNull TauChannel channel) throws DatabaseException {
        UUID chatID = channel.id();

        try (Connection conn = dataSource.getConnection()) {
            byte[] chatIDBin = UUIDUtil.uuidToBinary(chatID);

            try (PreparedStatement stmt = conn.prepareStatement("""
                SELECT token_id, expires_at, nickname, sender_nickname, code, created_at, activated_at
                FROM invite_codes
                WHERE chat_id = ? AND expires_at > NOW()
                ORDER BY expires_at DESC
            """)) {
                stmt.setBytes(1, chatIDBin);

                List<InviteCodeObject> tokens = new ArrayList<>();
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        byte[] tokenIdBin = rs.getBytes("token_id");
                        UUID tokenId = UUIDUtil.binaryToUUID(tokenIdBin);

                        ZonedDateTime expiresAt = rs
                                .getTimestamp("expires_at").toInstant()
                                .atZone(ZonedDateTime.now().getZone());

                        String nickname = rs.getString("nickname");
                        String senderNickname = rs.getString("sender_nickname");
                        String code = rs.getString("code");

                        ZonedDateTime createdAt = rs
                                .getTimestamp("created_at").toInstant()
                                .atZone(ZonedDateTime.now().getZone());

                        @Nullable Timestamp activatedTimestamp = rs.getTimestamp("activated_at");
                        ZonedDateTime activatedAt = null;
                        if (activatedTimestamp != null) {
                            activatedAt = activatedTimestamp.toInstant().atZone(ZonedDateTime.now().getZone());
                        }

                        InviteCodeObject token = new InviteCodeObject(this, tokenId, createdAt,
                                code, chatID, nickname, senderNickname, expiresAt, activatedAt);
                        tokens.add(token);
                    }
                }
                return tokens;
            }
        } catch (SQLException | IllegalArgumentException e) {
            throw new DatabaseException("Failed to retrieve active invite links", e);
        }
    }

    @Override
    public List<InviteCodeObject> getInviteCodesByNickname(Member member) throws DatabaseException {
        try (Connection conn = dataSource.getConnection()) {
            try (PreparedStatement stmt = conn.prepareStatement("""
                SELECT token_id, expires_at, chat_id, sender_nickname, code, created_at, activated_at
                FROM invite_codes
                WHERE nickname = ?
            """)) {
                stmt.setString(1, member.nickname());

                List<InviteCodeObject> tokens = new ArrayList<>();
                try (ResultSet rs = stmt.executeQuery()) {
                    while (rs.next()) {
                        byte[] tokenIdBin = rs.getBytes("token_id");
                        UUID tokenId = UUIDUtil.binaryToUUID(tokenIdBin);

                        ZonedDateTime expiresAt = rs
                                .getTimestamp("expires_at").toInstant()
                                .atZone(ZonedDateTime.now().getZone());

                        byte[] chatIdBin = rs.getBytes("chat_id");
                        UUID chatId = UUIDUtil.binaryToUUID(chatIdBin);

                        String senderNickname = rs.getString("sender_nickname");
                        String code = rs.getString("code");

                        ZonedDateTime createdAt = rs
                                .getTimestamp("created_at").toInstant()
                                .atZone(ZonedDateTime.now().getZone());

                        Timestamp activatedTimestamp = rs.getTimestamp("activated_at");
                        ZonedDateTime activatedAt = null;
                        if (activatedTimestamp != null) {
                            activatedAt = activatedTimestamp.toInstant().atZone(ZonedDateTime.now().getZone());
                        }

                        var token = new InviteCodeObject(this, tokenId, createdAt,
                                code, chatId, member.nickname(), senderNickname, expiresAt, activatedAt);
                        tokens.add(token);
                    }
                }
                return tokens;
            }
        } catch (SQLException | IllegalArgumentException e) {
            throw new DatabaseException("Failed to retrieve invite links by nickname", e);
        }
    }

    @Override
    public int countActiveInvitesByNickname(String nickname) throws DatabaseException {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement("""
                 SELECT COUNT(*) FROM invite_codes
                 WHERE nickname = ? AND expires_at > NOW()
             """)) {

            stmt.setString(1, nickname);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
                return 0;
            }
        } catch (SQLException e) {
            throw new DatabaseException("Failed to count active invites by nickname", e);
        }
    }

    @Override
    public void saveReactionType(ReactionType reaction) throws DatabaseException, AlreadyExistingRecordException {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement("""
                 INSERT INTO reaction_types (reaction_type_id, package_name, name, created_at)
                 VALUES (?, ?, ?, ?)
             """)) {

            stmt.setBytes(1, UUIDUtil.uuidToBinary(reaction.id()));
            stmt.setString(2, reaction.packageName());
            stmt.setString(3, reaction.name());
            stmt.setTimestamp(4, Timestamp.from(reaction.getCreationDate().toInstant()));

            int rowsInserted = stmt.executeUpdate();
            if (rowsInserted == 0) {
                throw new AlreadyExistingRecordException("Reaction type");
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error saving reaction type", e);
        }
    }

    @Override
    public void removeReactionType(ReactionType reaction) throws DatabaseException {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement("""
                DELETE FROM reaction_types WHERE reaction_type_id = ?
            """)) {
            stmt.setBytes(1, UUIDUtil.uuidToBinary(reaction.id()));
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("Failed to remove reaction type", e);
        }
    }

    @Override
    public void saveReactionEntry(ReactionEntry reaction) throws DatabaseException, AlreadyExistingRecordException {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement("""
                 INSERT INTO reaction_entries (reaction_entry_id, message_id, reaction_type_id, nickname, created_at)
                 VALUES (?, ?, ?, ?, ?)
             """)) {

            stmt.setBytes(1, UUIDUtil.uuidToBinary(reaction.id()));
            stmt.setBytes(2, UUIDUtil.uuidToBinary(reaction.messageId()));
            stmt.setBytes(3, UUIDUtil.uuidToBinary(reaction.reactionTypeId()));
            stmt.setString(4, reaction.nickname());
            stmt.setTimestamp(5, Timestamp.from(reaction.getCreationDate().toInstant()));

            int rowsInserted = stmt.executeUpdate();
            if (rowsInserted == 0) {
                throw new AlreadyExistingRecordException("Reaction entry");
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error saving reaction entry", e);
        }
    }

    @Override
    public void removeReactionEntry(ReactionEntry reaction) throws DatabaseException {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement("""
                DELETE FROM reaction_entries WHERE reaction_entry_id = ?
            """)) {
            stmt.setBytes(1, UUIDUtil.uuidToBinary(reaction.id()));
            stmt.executeUpdate();
        } catch (SQLException e) {
            throw new DatabaseException("Failed to remove reaction entry", e);
        }
    }

    @Override
    public Optional<ReactionType> getReactionTypeByName(String name) throws DatabaseException {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement("""
                 SELECT * FROM reaction_types
                 WHERE name = ?
             """)) {

            stmt.setString(1, name);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    UUID reactionTypeId = UUIDUtil.binaryToUUID(rs.getBytes("reaction_type_id"));
                    String packageName = rs.getString("package_name");
                    ZonedDateTime createdAt = rs.getTimestamp("created_at").toInstant().atZone(ZoneId.of("UTC"));
                    return Optional.of(new ReactionType(this, reactionTypeId, createdAt, name, packageName));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error fetching reaction type by name", e);
        }
        return Optional.empty();
    }

    @Override
    public List<ReactionType> getReactionTypesByPackage(String packageName) throws DatabaseException {
        List<ReactionType> reactionTypes = new ArrayList<>();

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement("""
                 SELECT * FROM reaction_types
                 WHERE package_name = ?
             """)) {

            stmt.setString(1, packageName);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    UUID reactionTypeId = UUIDUtil.binaryToUUID(rs.getBytes("reaction_type_id"));
                    String name = rs.getString("name");
                    ZonedDateTime createdAt = rs.getTimestamp("created_at").toInstant().atZone(ZoneId.of("UTC"));
                    reactionTypes.add(new ReactionType(this, reactionTypeId, createdAt, name, packageName));
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error fetching reaction types by package", e);
        }
        return reactionTypes;
    }

    @Override
    public List<ReactionEntry> getReactionsByMessage(ChatMessageViewDTO message) throws DatabaseException {
        List<ReactionEntry> reactions = new ArrayList<>();

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement("""
                 SELECT * FROM reaction_entries
                 WHERE message_id = ?
             """)) {

            stmt.setBytes(1, UUIDUtil.uuidToBinary(message.id()));

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    UUID reactionEntryId = UUIDUtil.binaryToUUID(rs.getBytes("reaction_entry_id"));
                    UUID reactionTypeId = UUIDUtil.binaryToUUID(rs.getBytes("reaction_type_id"));
                    String nickname = rs.getString("nickname");
                    ZonedDateTime createdAt = rs.getTimestamp("created_at").toInstant().atZone(ZoneId.of("UTC"));
                    ReactionEntry re = new ReactionEntry(
                            this, reactionEntryId, createdAt,
                            message.id(), reactionTypeId, nickname
                    );
                    reactions.add(re);
                }
            }
        } catch (SQLException e) {
            throw new DatabaseException("Error fetching reactions by message", e);
        }
        return reactions;
    }
}