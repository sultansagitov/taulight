package net.result.sandnode.exceptions;

public class NoSuchReqHandler extends SandnodeException {

    public NoSuchReqHandler(byte b) {
        super("Can't find MessageType for " + String.format("%8s", Integer.toBinaryString(b & 0xFF)).replace(' ', '0'));
    }

    public NoSuchReqHandler(Exception e) {
        super(e);
    }

}
