package net.result.sandnode.config;

import com.auth0.jwt.algorithms.Algorithm;

public interface JWTConfig {
    Algorithm getAlgorithm();
}
