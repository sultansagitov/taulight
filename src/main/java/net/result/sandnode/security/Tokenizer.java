package net.result.sandnode.security;

import net.result.sandnode.entity.LoginEntity;

import java.util.Optional;

public interface Tokenizer {
    String tokenizeLogin(LoginEntity login);

    Optional<LoginEntity> findLogin(String token);
}
