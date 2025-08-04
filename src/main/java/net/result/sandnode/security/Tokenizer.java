package net.result.sandnode.security;

import net.result.sandnode.db.LoginEntity;
import net.result.sandnode.exception.DatabaseException;
import net.result.sandnode.exception.error.ExpiredTokenException;
import net.result.sandnode.exception.error.InvalidArgumentException;

import java.util.Optional;

public interface Tokenizer {
    String tokenizeLogin(LoginEntity login);

    Optional<LoginEntity> findLogin(String token)
            throws ExpiredTokenException, InvalidArgumentException, DatabaseException;
}
