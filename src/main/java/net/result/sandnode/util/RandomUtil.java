package net.result.sandnode.util;

import org.jetbrains.annotations.NotNull;

import java.security.SecureRandom;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class RandomUtil {
    public static @NotNull String getRandomString() {
        Random random = new SecureRandom();
        String s = "0123456789abcdefghijklmnopqrstuvwxyz!@$%&*()_+-={}[]\"'<>?,./ ~";
        return IntStream
                .range(0, random.nextInt(16, 32))
                .mapToObj(i -> "" + s.charAt(random.nextInt(61)))
                .collect(Collectors.joining());
    }
}
