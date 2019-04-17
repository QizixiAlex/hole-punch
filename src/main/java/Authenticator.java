import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 Authenticator for Punch Server List & Connect Commands
* */
public class Authenticator {

    private List<UserInfo> userInfoList;
    private SecurityUtils security;

    public Authenticator(String filePath) {
        userInfoList = new ArrayList<>();
        security = new SecurityUtils();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            int counter = 0;
            UserInfo info = new UserInfo();
            while ((line = br.readLine()) != null) {
                switch (counter) {
                    case 0:
                        info.userName = line;
                        break;
                    case 1:
                        info.portNum = Integer.parseInt(line);
                        break;
                    case 2:
                        info.hashedPassword = line;
                        break;
                    case 3:
                        info.salt = line;
                        userInfoList.add(info);
                        info = new UserInfo();
                        break;
                }
                counter++;
                counter = counter % 4;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //authenticate connect command
    public boolean authenticate(String userName, String password, int portNum) {
        for (UserInfo info : userInfoList) {
            if (userName.equals(info.userName) && portNum == info.portNum) {
                //check password hash
                String hashPassword = security.hashPassword(password, info.salt)[0];
                if (hashPassword.equals(info.hashedPassword)) {
                    return true;
                }
            }
        }
        return false;
    }

    //authenticate list command
    public boolean authenticate(String userName, String password) {
        for (UserInfo info : userInfoList) {
            if (userName.equals(info.userName)) {
                //check password hash
                String hashPassword = security.hashPassword(password, info.salt)[0];
                if (hashPassword.equals(info.hashedPassword)) {
                    return true;
                }
            }
        }
        return false;
    }
}
