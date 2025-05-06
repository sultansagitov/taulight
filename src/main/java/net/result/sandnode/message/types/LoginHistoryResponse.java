package net.result.sandnode.message.types;

import com.fasterxml.jackson.core.type.TypeReference;
import net.result.sandnode.db.LoginEntity;
import net.result.sandnode.dto.LoginHistoryDTO;
import net.result.sandnode.exception.DeserializationException;
import net.result.sandnode.exception.ExpectedMessageException;
import net.result.sandnode.message.MSGPackMessage;
import net.result.sandnode.message.RawMessage;
import net.result.sandnode.message.util.Headers;
import net.result.sandnode.message.util.MessageTypes;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;

public class LoginHistoryResponse extends MSGPackMessage<List<LoginHistoryDTO>> {
    public LoginHistoryResponse(@NotNull Headers headers, @NotNull Collection<LoginEntity> entities) {
        super(headers.setType(MessageTypes.LOGIN), entities.stream().map(LoginHistoryDTO::new).toList());
    }

    public LoginHistoryResponse(@NotNull RawMessage raw) throws DeserializationException, ExpectedMessageException {
        super(raw.expect(MessageTypes.LOGIN), new TypeReference<>() {});
    }

    public List<LoginHistoryDTO> history() {
        return object;
    }
}
