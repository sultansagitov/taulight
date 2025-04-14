package net.result.taulight.db;

import net.result.sandnode.db.JPAUtil;
import net.result.sandnode.db.MemberEntity;
import net.result.sandnode.security.PasswordHashers;
import net.result.taulight.dto.ChatMessageInputDTO;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;

public class CascadingTest {

    private static TauDatabase database;

    @BeforeAll
    public static void setup() {
        JPAUtil.buildEntityManagerFactory();
        database = new TauJPADatabase(PasswordHashers.BCRYPT);
    }

    @Test
    public void testLeaveFromChannel() throws Exception {
        MemberEntity m1 = database.registerMember("new_user", "pass");
        TauMemberEntity tau = m1.tauMember();

        ChannelEntity channel = database.createChannel("news", tau);

        // Пригласим того же пользователя (да, он уже владелец, но проверим добавление)
        boolean added = database.addMemberToChannel(channel, tau);
        assertFalse(added); // Он уже там как владелец

        // Попробуем убрать из канала
        boolean left = database.leaveFromChannel(channel, tau);
        assertTrue(left); // Должно успешно удалить

        // Повторная попытка — уже не должен быть участником
        boolean leftAgain = database.leaveFromChannel(channel, tau);
        assertFalse(leftAgain);
    }

    @Test
    public void testRemoveReactionByObject() throws Exception {
        MemberEntity m1 = database.registerMember("reacter", "123");
        MemberEntity m2 = database.registerMember("author", "123");

        TauMemberEntity reacter = m1.tauMember();
        TauMemberEntity author = m2.tauMember();

        ChatEntity chat = database.createChannel("memes", author);
        ChatMessageInputDTO input = new ChatMessageInputDTO()
                .setContent("Hello world")
                .setChat(chat)
                .setMember(m1)
                .setSentDatetimeNow()
                .setRepliedToMessages(new HashSet<>())
                .setSys(true);
        MessageEntity msg = database.createMessage(chat, input, author);

        ReactionTypeEntity like = database.createReactionType("Like", "basic");
        ReactionEntryEntity entry = database.createReactionEntry(reacter, msg, like);

        // Удалим по объекту
        boolean removed = database.removeReactionEntry(entry);
        assertTrue(removed);

        // Повторно — уже не существует
        boolean removedAgain = database.removeReactionEntry(entry);
        assertFalse(removedAgain);
    }

    @Test
    public void testRemoveReactionByCompositeKey() throws Exception {
        MemberEntity m1 = database.registerMember("maria", "123");
        MemberEntity m2 = database.registerMember("mark", "123");

        TauMemberEntity reacter = m1.tauMember();
        TauMemberEntity author = m2.tauMember();

        ChatEntity chat = database.createChannel("random", author);
        ChatMessageInputDTO input = new ChatMessageInputDTO()
                .setContent("Hello world")
                .setChat(chat)
                .setMember(m1)
                .setSentDatetimeNow()
                .setRepliedToMessages(new HashSet<>())
                .setSys(true);
        MessageEntity msg = database.createMessage(chat, input, author);

        ReactionTypeEntity haha = database.createReactionType("Haha", "basic");
        database.createReactionEntry(reacter, msg, haha);

        // Удалим через message+member+reactionType
        boolean removed = database.removeReactionEntry(msg, reacter, haha);
        assertTrue(removed);

        // Повторно — уже не существует
        boolean removedAgain = database.removeReactionEntry(msg, reacter, haha);
        assertFalse(removedAgain);
    }

    @Test
    public void testActivateInviteCode() throws Exception {
        MemberEntity sender = database.registerMember("sender", "pass");
        MemberEntity receiver = database.registerMember("receiver", "pass");
        TauMemberEntity s = sender.tauMember();
        TauMemberEntity r = receiver.tauMember();

        ChannelEntity channel = database.createChannel("private", s);
        InviteCodeEntity invite = database.createInviteCode(channel, r, s, ZonedDateTime.now().plusDays(1));

        assertTrue(database.activateInviteCode(invite)); // первый раз — успех
        assertFalse(database.activateInviteCode(invite)); // повторно — уже использован
    }

}
