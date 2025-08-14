package net.result.sandnode.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import net.result.sandnode.config.JWTConfig;
import net.result.sandnode.entity.LoginEntity;
import net.result.sandnode.exception.DatabaseException;
import net.result.sandnode.exception.error.ExpiredTokenException;
import net.result.sandnode.exception.error.InvalidArgumentException;
import net.result.sandnode.util.Container;
import net.result.sandnode.db.JPAUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.util.Date;
import java.util.Optional;
import java.util.UUID;

public class JWTTokenizer implements Tokenizer {
    private static final Logger LOGGER = LogManager.getLogger(JWTTokenizer.class);
    private final JWTVerifier VERIFIER;
    private final JWTConfig jwtConfig;
    private final JPAUtil jpaUtil;

    public JWTTokenizer(@NotNull Container container, @NotNull JWTConfig jwtConfig) {
        this.jwtConfig = jwtConfig;
        VERIFIER = JWT.require(jwtConfig.getAlgorithm()).build();
        jpaUtil = container.get(JPAUtil.class);
    }

    @Override
    public String tokenizeLogin(@NotNull LoginEntity login) {
        long EXPIRATION_TIME_MS = 3600 * 1000 * 24;
        return JWT.create()
                .withSubject(login.id().toString())
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + EXPIRATION_TIME_MS))
                .sign(jwtConfig.getAlgorithm());
    }

    @Override
    public Optional<LoginEntity> findLogin(String token)
            throws ExpiredTokenException, InvalidArgumentException, DatabaseException {
        try {
            DecodedJWT decodedJWT = VERIFIER.verify(token);
            String uuid = decodedJWT.getSubject();
            return jpaUtil.find(LoginEntity.class, UUID.fromString(uuid));
        } catch (TokenExpiredException e) {
            LOGGER.error("Expired token", e);
            throw new ExpiredTokenException(e);
        } catch (JWTVerificationException | IllegalArgumentException e) {
            LOGGER.error("Invalid token", e);
            throw new InvalidArgumentException(e);
        }
    }
}
