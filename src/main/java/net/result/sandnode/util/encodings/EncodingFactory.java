package net.result.sandnode.util.encodings;

import net.result.sandnode.exceptions.NoSuchEncoderException;
import net.result.sandnode.util.encodings.base64.Base64Decoder;
import net.result.sandnode.util.encodings.base64.Base64Encoder;
import net.result.sandnode.util.encodings.hex.HexDecoder;
import net.result.sandnode.util.encodings.hex.HexEncoder;
import net.result.sandnode.util.encodings.interfaces.IDecoder;
import net.result.sandnode.util.encodings.interfaces.IEncoder;
import net.result.sandnode.util.encodings.no.NoDecoder;
import net.result.sandnode.util.encodings.no.NoEncoder;
import org.jetbrains.annotations.NotNull;

public class EncodingFactory {
    public static @NotNull IEncoder getEncoder(@NotNull String encoderName) throws NoSuchEncoderException {
        final String ENC = encoderName.toLowerCase();
        return switch (ENC) {
            case "base64" -> new Base64Encoder();
            case "hex" -> new HexEncoder();
            case "no" -> new NoEncoder();
            default -> throw new NoSuchEncoderException("Cannot find encoding named %s".formatted(encoderName));
        };
    }

    public static @NotNull IDecoder getDecoder(@NotNull String decoderName) throws NoSuchEncoderException {
        final String DEC = decoderName.toLowerCase();
        return switch (DEC) {
            case "base64" -> new Base64Decoder();
            case "hex" -> new HexDecoder();
            case "no" -> new NoDecoder();
            default -> throw new NoSuchEncoderException("Cannot find encoding named %s".formatted(decoderName));
        };
    }
}
