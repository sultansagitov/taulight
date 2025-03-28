package net.result.sandnode.security;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PasswordHashersTest {
    @Test
    void testHashAndVerify() {
        String password = "securePassword123";
        int workload = 12;

        for (PasswordHasher hasher : PasswordHashers.values()) {
            String hashedPassword = hasher.hash(password, workload);
            assertNotNull(hashedPassword, "Hashed password should not be null");
            assertNotEquals(password, hashedPassword, "Hashed password should not match raw password");

            assertTrue(hasher.verify(password, hashedPassword), "Password should be verified correctly");
        }
    }

    @Test
    void testVerifyWithIncorrectPassword() {
        String password = "correctPassword";
        String incorrectPassword = "wrongPassword";
        int workload = 12;

        for (PasswordHasher hasher : PasswordHashers.values()) {
            String hashedPassword = hasher.hash(password, workload);
            assertNotNull(hashedPassword, "Hashed password should not be null");

            assertFalse(hasher.verify(incorrectPassword, hashedPassword), "Incorrect password should not be verified");
        }
    }
}

