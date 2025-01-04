package net.result.sandnode.messages;

import com.fasterxml.jackson.core.JsonProcessingException;
import net.result.sandnode.exceptions.DeserializationException;
import net.result.sandnode.messages.util.Headers;
import org.jetbrains.annotations.NotNull;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.msgpack.jackson.dataformat.MessagePackFactory;

import java.io.IOException;

public abstract class MSGPackMessage<T> extends Message {
    private static final ObjectMapper objectMapper;

    static  {
        objectMapper = new ObjectMapper(new MessagePackFactory());
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    public final T object;

    public MSGPackMessage(@NotNull Headers headers, T object) {
        super(headers);
        this.object = object;
    }

    public MSGPackMessage(@NotNull IMessage message, Class<T> clazz) throws DeserializationException {
        super(message.getHeaders());
        try {
            object = objectMapper.readValue(message.getBody(), clazz);
        } catch (IOException e) {
            throw new DeserializationException(e);
        }
    }

    @Override
    public byte[] getBody() {
        try {
            return objectMapper.writeValueAsBytes(object);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
