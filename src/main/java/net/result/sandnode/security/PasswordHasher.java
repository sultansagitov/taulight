package net.result.sandnode.security;

public interface PasswordHasher {
    String hash(String password, int wayload);

    boolean verify(String password, String hash);
}
