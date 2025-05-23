package net.result.sandnode.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import net.result.sandnode.config.JWTConfig;
import net.result.sandnode.exception.DatabaseException;
import net.result.sandnode.exception.error.ExpiredTokenException;
import net.result.sandnode.exception.error.InvalidTokenException;
import net.result.sandnode.db.Database;
import net.result.sandnode.db.MemberEntity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.util.Date;
import java.util.Optional;

public class JWTTokenizer implements Tokenizer {
    private static final Logger LOGGER = LogManager.getLogger(JWTTokenizer.class);
    private final JWTVerifier VERIFIER;
    private final JWTConfig jwtConfig;

    public JWTTokenizer(@NotNull JWTConfig jwtConfig) {
        this.jwtConfig = jwtConfig;
        VERIFIER = JWT.require(jwtConfig.getAlgorithm()).build();
    }

    @Override
    public String tokenizeMember(@NotNull MemberEntity member) {
        long EXPIRATION_TIME_MS = 3600 * 1000 * 24;
        return JWT.create()
                .withSubject(member.nickname())
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + EXPIRATION_TIME_MS))
                .sign(jwtConfig.getAlgorithm());
    }

    @Override
    public Optional<MemberEntity> findMember(@NotNull Database database, @NotNull String token)
            throws InvalidTokenException, ExpiredTokenException, DatabaseException {
        try {
            DecodedJWT decodedJWT = VERIFIER.verify(token);
            String nickname = decodedJWT.getSubject();
            return database.findMemberByNickname(nickname);
        } catch (TokenExpiredException e) {
            LOGGER.error("Expired token", e);
            throw new ExpiredTokenException(e);
        } catch (JWTVerificationException e) {
            LOGGER.error("Invalid token", e);
            throw new InvalidTokenException(e);
        }
    }
}
