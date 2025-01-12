package net.result.taulight.messages;


//@JsonSerialize(using = DataType.DataTypeSerializer.class)
//@JsonDeserialize(using = DataType.DataTypeDeserializer.class)
public enum DataType {
    GET,
    ADD,
    REMOVE,
    WRITE;

//    public static class DataTypeSerializer extends JsonSerializer<DataType> {
//        @Override
//        public void serialize(DataType value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
//            gen.writeString(value.name());
//        }
//    }
//
//    public static class DataTypeDeserializer extends JsonDeserializer<DataType> {
//        @Override
//        public DataType deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
//            String value = p.getText();
//            return DataType.valueOf(value.toUpperCase());
//        }
//    }
}
