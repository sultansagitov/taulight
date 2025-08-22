package net.result.sandnode.exception;

public class SandnodeInterruptedException extends SandnodeException {
    public SandnodeInterruptedException(InterruptedException e) {
        super(e);
    }
}
