package com.example.salesmanager4.users;

import java.security.SecureRandom;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class PasswordGenerator {
    private static final String CHAR_LOWER = "abcdefghijklmnopqrstuvwxyz";
    private static final String CHAR_UPPER = CHAR_LOWER.toUpperCase();
    private static final String NUMBER = "0123456789";
    private static final String OTHER_CHAR = "!@#$%&*()_+-=[]?";
    private static final String PASSWORD_ALLOW_BASE = CHAR_LOWER + CHAR_UPPER + NUMBER + OTHER_CHAR;
    private static final SecureRandom random = new SecureRandom();

    public static String generateTemporaryPassword(int length) {
        if (length < 8) length = 8;

        // Ensure at least one of each category for complexity
        StringBuilder sb = new StringBuilder(length);
        sb.append(CHAR_LOWER.charAt(random.nextInt(CHAR_LOWER.length())));
        sb.append(CHAR_UPPER.charAt(random.nextInt(CHAR_UPPER.length())));
        sb.append(NUMBER.charAt(random.nextInt(NUMBER.length())));
        sb.append(OTHER_CHAR.charAt(random.nextInt(OTHER_CHAR.length())));

        // Fill the rest randomly
        for (int i = 4; i < length; i++) {
            sb.append(PASSWORD_ALLOW_BASE.charAt(random.nextInt(PASSWORD_ALLOW_BASE.length())));
        }

        // Shuffle the characters so the fixed categories aren't always at the start
        List<Character> letters = sb.chars().mapToObj(c -> (char) c).collect(Collectors.toList());
        Collections.shuffle(letters);
        
        return letters.stream().map(String::valueOf).collect(Collectors.joining());
    }
}
