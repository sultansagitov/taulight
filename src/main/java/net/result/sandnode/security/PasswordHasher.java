package net.result.sandnode.security;

public interface PasswordHasher {
    String hash(String password);

    boolean verify(String password, String hash);
}
