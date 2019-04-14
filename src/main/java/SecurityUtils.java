import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

public class SecurityUtils {

    private MessageDigest md5;
    public static final int PASSWORD_LENGTH = 32;
    public static final int SALT_LENGTH = 16;

    public SecurityUtils() {
        try {
            this.md5 = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException ignored) {}
    }

    private String generateSalt() {
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
        byte[] messageDigest = md5.digest(password.getBytes());
        BigInteger bi = new BigInteger(1, messageDigest);
        String passwordHash = bi.toString(16);
        passwordHash = passwordHash.replaceAll("\\s+", "");
        while (passwordHash.length() < PASSWORD_LENGTH) {
            passwordHash = passwordHash + "0";
        }
        return new String[]{passwordHash, salt};
    }


}
