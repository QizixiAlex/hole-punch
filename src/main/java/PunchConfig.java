import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Scanner;

public class PunchConfig {

    private static final String userFileName = System.getenv("HOME") + "/.hole_punch/users";
    private SecurityUtils security;

    public PunchConfig() {
        this.security = new SecurityUtils();
    }

    public boolean addConfig() throws IOException {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter username:");
        String userName = scanner.nextLine();
        System.out.println("Enter port number:");
        int portNum = Integer.parseInt(scanner.nextLine());
        System.out.println("Enter password:");
        String password = scanner.nextLine();
        if (userName.length() == 0 || password.length() == 0) {
            System.out.println("invalid input");
            return false;
        }
        String[] hashResult = security.hashPassword(password, null);
        PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(userFileName, true)));
        writer.println(userName);
        writer.println(portNum);
        writer.println(hashResult[0]);
        writer.println(hashResult[1]);
        writer.close();
        return true;
    }

    public static void main(String[] args) {
        PunchConfig pc = new PunchConfig();
        try {
            while (!pc.addConfig()) {
                //if input username and password is invalid, prompt again
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
