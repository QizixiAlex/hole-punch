import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

/**
 * Securely store user password with SHA-256 algorithm and add salt
 * **/
public class SecurityUtils {

    private MessageDigest sha256;
    public static final int PASSWORD_LENGTH = 32;
    public static final int SALT_LENGTH = 16;

    public SecurityUtils() {
        try {
            this.sha256 = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException ignored) {}
    }

    public String generateSalt() {
        StringBuilder saltBuilder = new StringBuilder();
        String saltCharset = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
        Random rand = new Random();
        for (int i=0 ; i< SALT_LENGTH ; i++) {
            int idx = (int)(rand.nextFloat()*saltCharset.length());
            saltBuilder.append(saltCharset.charAt(idx));
        }
        return saltBuilder.toString();
    }

    public String[] hashPassword(String password, String salt) {
        if (salt == null) {
            salt = generateSalt();
        }
        password = password + salt;
        byte[] messageDigest = sha256.digest(password.getBytes(StandardCharsets.UTF_8));
        BigInteger bi = new BigInteger(1, messageDigest);
        String passwordHash = bi.toString(16);
        passwordHash = passwordHash.replaceAll("\\s+", "");
        while (passwordHash.length() < PASSWORD_LENGTH) {
            passwordHash = passwordHash + "0";
        }
        return new String[]{passwordHash, salt};
    }


}
