package net.result.sandnode.security;

public interface PasswordHasher {
    String hash(String password, int workload);

    boolean verify(String password, String hash);
}
