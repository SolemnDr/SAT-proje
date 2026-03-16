package util;

import org.mindrot.jbcrypt.BCrypt;

public class PasswordUtil {
    public static String hash(String plain) {
        return BCrypt.hashpw(plain, BCrypt.gensalt());
    }

    public static boolean verify(String plain, String hash) {
        return BCrypt.checkpw(plain, hash);
    }
}