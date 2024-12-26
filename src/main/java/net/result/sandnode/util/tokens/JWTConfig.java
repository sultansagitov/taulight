package net.result.sandnode.util.tokens;

import com.auth0.jwt.algorithms.Algorithm;

public class JWTConfig {
    public final Algorithm ALGORITHM;

    public JWTConfig(String secretKey) {
        ALGORITHM = Algorithm.HMAC256(secretKey);
    }
}
