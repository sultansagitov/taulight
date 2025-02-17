package net.result.sandnode.exception;

public class ServerSandnodeErrorException extends SandnodeErrorException {
    public ServerSandnodeErrorException() {
        super();
    }

    public ServerSandnodeErrorException(int code) {
        super(String.valueOf(code));
    }
}
