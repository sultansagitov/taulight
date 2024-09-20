package net.result.sandnode.exceptions;

public class NoSuchReqHandler extends SandnodeException {

    public NoSuchReqHandler(String message) {
        super(message);
    }

    public NoSuchReqHandler(Exception e) {
        super(e);
    }

}
