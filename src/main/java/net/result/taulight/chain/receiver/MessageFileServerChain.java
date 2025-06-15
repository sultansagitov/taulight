package net.result.taulight.chain.receiver;

import net.result.sandnode.chain.ReceiverChain;
import net.result.sandnode.chain.ServerChain;
import net.result.sandnode.db.FileEntity;
import net.result.sandnode.dto.FileDTO;
import net.result.sandnode.exception.error.NotFoundException;
import net.result.sandnode.exception.error.TooFewArgumentsException;
import net.result.sandnode.exception.error.UnauthorizedException;
import net.result.sandnode.message.UUIDMessage;
import net.result.sandnode.message.types.FileMessage;
import net.result.sandnode.message.util.Headers;
import net.result.sandnode.message.util.MessageTypes;
import net.result.sandnode.serverclient.Session;
import net.result.sandnode.util.DBFileUtil;
import net.result.taulight.db.ChatEntity;
import net.result.taulight.db.MessageFileEntity;
import net.result.taulight.db.MessageFileRepository;
import net.result.taulight.db.TauMemberEntity;
import net.result.taulight.message.types.MessageFileRequest;
import net.result.taulight.util.ChatUtil;

import java.util.UUID;

public class MessageFileServerChain extends ServerChain implements ReceiverChain {
    public MessageFileServerChain(Session session) {
        super(session);
    }

    @Override
    public void sync() throws Exception {
        if (session.member == null) throw new UnauthorizedException();

        MessageFileRequest request = new MessageFileRequest(queue.take());
        UUID chatID = request.chatID;
        UUID fileID = request.fileID;

        if (chatID != null) {
            String filename = request.filename;
            if (filename == null) throw new TooFewArgumentsException();
            uploadFile(chatID, filename, session.member.tauMember());
        } else if (fileID != null) {
            downloadFile(fileID);
        } else {
            throw new TooFewArgumentsException();
        }
    }

    private void uploadFile(UUID chatID, String originalName, TauMemberEntity you) throws Exception {
        FileMessage fileMessage = new FileMessage(queue.take());

        FileDTO dto = fileMessage.dto();

        MessageFileRepository messageFileRepo = session.server.container.get(MessageFileRepository.class);
        ChatUtil chatUtil = session.server.container.get(ChatUtil.class);
        DBFileUtil dbFileUtil = session.server.container.get(DBFileUtil.class);

        ChatEntity chat = chatUtil.getChat(chatID).orElseThrow(NotFoundException::new);

        String filename = "%s%s".formatted(chatID, UUID.randomUUID());
        FileEntity fileEntity = dbFileUtil.saveFile(dto, filename);

        MessageFileEntity entity = messageFileRepo.create(you, chat, originalName, fileEntity);

        send(new UUIDMessage(new Headers().setType(MessageTypes.HAPPY), entity));
    }

    private void downloadFile(UUID fileID) throws Exception {
        MessageFileRepository messageFileRepo = session.server.container.get(MessageFileRepository.class);
        DBFileUtil dbFileUtil = session.server.container.get(DBFileUtil.class);

        MessageFileEntity fileEntity = messageFileRepo.find(fileID).orElseThrow(NotFoundException::new);

        FileDTO fileDTO = dbFileUtil.readImage(fileEntity.file());

        send(new FileMessage(new Headers().setType(MessageTypes.FILE), fileDTO));
    }

}
