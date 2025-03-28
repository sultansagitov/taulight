package net.result.sandnode.security;

import org.mindrot.jbcrypt.BCrypt;

public enum PasswordHashers implements PasswordHasher {
    BCRYPT {
        @Override
        public String hash(String password, int workload) {
            return BCrypt.hashpw(password, BCrypt.gensalt(workload));
        }

        @Override
        public boolean verify(String password, String hash) {
            return BCrypt.checkpw(password, hash);
        }
    }
}

