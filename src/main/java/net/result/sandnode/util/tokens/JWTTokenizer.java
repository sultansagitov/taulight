package net.result.sandnode.util.tokens;

import com.auth0.jwt.JWT;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import net.result.sandnode.exceptions.InvalidTokenException;
import net.result.sandnode.util.db.IDatabase;
import net.result.sandnode.util.db.IMember;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.NotNull;

import java.util.Date;
import java.util.Optional;

public class JWTTokenizer implements ITokenizer {
    private static final Logger LOGGER = LogManager.getLogger(JWTTokenizer.class);
    private final JWTVerifier VERIFIER;
    private final JWTConfig jwtConfig;

    public JWTTokenizer(@NotNull JWTConfig jwtConfig) {
        this.jwtConfig = jwtConfig;
        VERIFIER = JWT.require(jwtConfig.ALGORITHM).build();
    }

    @Override
    public String tokenizeMember(@NotNull IMember member) {
        long EXPIRATION_TIME_MS = 3600 * 1000;
        return JWT.create()
                .withSubject(member.getID())
                .withClaim("hashedPassword", member.getHashedPassword())
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + EXPIRATION_TIME_MS))
                .sign(jwtConfig.ALGORITHM);
    }

    @Override
    public Optional<IMember> findMember(
            @NotNull IDatabase database,
            @NotNull String token
    ) throws InvalidTokenException {
        try {
            DecodedJWT decodedJWT = VERIFIER.verify(token);
            String memberId = decodedJWT.getSubject();
            return database.findMemberByMemberID(memberId);
        } catch (JWTVerificationException e) {
            LOGGER.debug("Invalid token", e);
            throw new InvalidTokenException(e);
        }
    }
}
