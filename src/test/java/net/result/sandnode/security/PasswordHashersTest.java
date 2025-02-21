package net.result.sandnode.security;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PasswordHashersTest {

    @Test
    void testHashAndVerify() {
        String password = "SecurePass123";
        PasswordHasher hasher = PasswordHashers.BCRYPT;

        String hashedPassword = hasher.hash(password);
        assertNotNull(hashedPassword);
        assertNotEquals(password, hashedPassword);

        assertTrue(hasher.verify(password, hashedPassword));
        assertFalse(hasher.verify("WrongPassword", hashedPassword));
    }

}
