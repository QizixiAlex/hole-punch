import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.List;

public class SecurityUtils {

    private final static int keyLength = 128;
    private final static int iterations = 65536;
    private final static String secretKeyAlgo = "PBKDF2WithHmacSHA512";
    private static final String userFileName = "users";

    //references https://medium.com/@kasunpdh/how-to-store-passwords-securely-with-pbkdf2-204487f14e84
    public static String hashPassword( final char[] password, final byte[] salt ) {
        try {
            SecretKeyFactory skf = SecretKeyFactory.getInstance(secretKeyAlgo);
            PBEKeySpec spec = new PBEKeySpec( password, salt, iterations, keyLength );
            SecretKey key = skf.generateSecret( spec );
            return new String(key.getEncoded(), Charset.forName("UTF-8"));
        } catch ( Exception e ) {
            throw new RuntimeException( e );
        }
    }

    public List<UserInfo> readExistingUsers() {
        List<UserInfo> results = new ArrayList<UserInfo>();
        try {
            BufferedReader br = new BufferedReader(new FileReader(userFileName));
            String line;
            while ((line = br.readLine()) != null) {
                String[] tokens = line.split("\\s+");
                String userName = tokens[0];
                int portNum = Integer.parseInt(tokens[1]);
                String hashedPassword = tokens[2];
                String saltStr = tokens[3];
                byte[] salt = saltStr.getBytes(Charset.forName("UTF-8"));
                results.add(new UserInfo(userName, hashedPassword, portNum, salt));
            }
        } catch (Exception ignored) {
        } finally {
            return results;
        }
    }

}
