package net.result.openhelo;

import net.result.openhelo.exceptions.WrongTypeException;
import net.result.openhelo.messages.*;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public enum HeloType {
    ECHO(0) {
        @Override
        public TextMessage fromBytes(byte[] bytes) {
            return new EchoMessage(new String(bytes));
        }
    },
    FORWARD(1) {
        @Override
        public ForwardMessage fromBytes(byte[] bytes) {
            return new ForwardMessage(new String(bytes));
        }
    },
    ONLINE(2) {
        @Override
        public HeloMessage fromBytes(byte[] bytes) {
            return new OnlineMessage();
        }
    },
    ONLINE_RESPONSE(3) {
        @Override
        public HeloMessage fromBytes(byte[] bytes) {
            return new OnlineResponseMessage(new String(bytes).split(","));
        }
    };

    private final byte type;

    HeloType(int type){
        this.type = (byte) type;
    }

    public byte asByte() {
        return type;
    }
    public abstract HeloMessage fromBytes(byte[] bytes);

    public static @NotNull HeloType fromByte(byte type) throws WrongTypeException {
        for (HeloType value : HeloType.values())
            if (value.asByte() == type)
                return value;

        throw new WrongTypeException(type);
    }
}
