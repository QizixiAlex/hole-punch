public class UserInfo {

    public String userName;
    public String hashedPassword;
    public int portNum;
    public byte[] salt;

    public UserInfo(String userName, String hashedPassword, int portNum, byte[] salt) {
        this.userName = userName;
        this.hashedPassword = hashedPassword;
        this.portNum = portNum;
        this.salt = salt;
    }
}
