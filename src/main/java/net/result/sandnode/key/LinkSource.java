package net.result.sandnode.key;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import net.result.sandnode.link.SandnodeLinkRecord;
import net.result.sandnode.message.util.NodeType;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.time.ZonedDateTime;

public class LinkSource extends Source {
    @JsonSerialize(using = SandnodeLinkRecordSerializer.class)
    @JsonDeserialize(using = SandnodeLinkRecordDeserializer.class)
    public SandnodeLinkRecord link;

    @SuppressWarnings("unused")
    public LinkSource() {}

    public LinkSource(@NotNull SandnodeLinkRecord link) {
        super(ZonedDateTime.now());
        this.link = link;
    }
}

class SandnodeLinkRecordSerializer extends JsonSerializer<SandnodeLinkRecord> {
    @Override
    public void serialize(SandnodeLinkRecord value, JsonGenerator gen, SerializerProvider ignored) throws IOException {
        gen.writeString(value.toString());
    }
}

class SandnodeLinkRecordDeserializer extends JsonDeserializer<SandnodeLinkRecord> {
    @Override
    public SandnodeLinkRecord deserialize(JsonParser p, DeserializationContext ignored) throws IOException {
        String text = p.getValueAsString();
        return SandnodeLinkRecord.parse(text, NodeType.HUB);
    }
}
