package net.result.sandnode.message;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import net.result.sandnode.exception.DeserializationException;
import net.result.sandnode.message.util.Headers;
import org.jetbrains.annotations.NotNull;
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
        super(message.headers());
        try {
            object = objectMapper.readValue(message.getBody(), clazz);
        } catch (IOException e) {
            throw new DeserializationException(e);
        }
    }

    public MSGPackMessage(@NotNull IMessage message, TypeReference<T> typeReference) throws DeserializationException {
        super(message.headers());
        try {
            object = objectMapper.readValue(message.getBody(), typeReference);
        } catch (IOException e) {
            throw new DeserializationException(e);
        }
    }

    public T dto() {
        return object;
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
