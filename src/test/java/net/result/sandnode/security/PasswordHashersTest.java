package net.result.sandnode.security;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class PasswordHashersTest {

    static @NotNull Stream<PasswordHasher> hashersProvider() {
        return Arrays.stream(PasswordHashers.values());
    }

    @ParameterizedTest
    @MethodSource("hashersProvider")
    void testHashAndVerify(@NotNull PasswordHasher hasher) {
        String password = "securePassword123";
        int workload = 12;

        String hashedPassword = hasher.hash(password, workload);
        assertNotNull(hashedPassword, "Hashed password should not be null");
        assertNotEquals(password, hashedPassword, "Hashed password should not match raw password");

        assertTrue(hasher.verify(password, hashedPassword), "Password should be verified correctly");
    }

    @ParameterizedTest
    @MethodSource("hashersProvider")
    void testVerifyWithIncorrectPassword(@NotNull PasswordHasher hasher) {
        String password = "correctPassword";
        String incorrectPassword = "wrongPassword";
        int workload = 12;

        String hashedPassword = hasher.hash(password, workload);
        assertNotNull(hashedPassword, "Hashed password should not be null");

        assertFalse(hasher.verify(incorrectPassword, hashedPassword), "Incorrect password should not be verified");
    }
}
