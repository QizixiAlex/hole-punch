import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

public class PunchConfig {

    private static final String userFileName = "users";

    public void writeToFile(String inputStr) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(userFileName));
        writer.write(inputStr);
        writer.close();
    }

    public void addConfig() {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter username:");
        String userName = scanner.nextLine();
        System.out.println("Enter port number:");
        int portNum = scanner.nextInt();
        System.out.println("Enter password:");
        String password = scanner.nextLine();
    }

    public static void main(String[] args) {
        PunchConfig pc = new PunchConfig();
        try {
            pc.writeToFile("Hi");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
