import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * PunchClient for calling the list command
 * **/

public class PunchClientListCommand {
    private int serverPort;
    private String serverHost;
    private String userName;
    private String password;

    public PunchClientListCommand(String serverHost, int serverPort, String userName, String password) {
        this.serverPort = serverPort;
        this.serverHost = serverHost;
        this.userName = userName;
        this.password = password;
    }

    public void start() {
        Socket controlSocket = null;
        try {
            //connect to ps
            controlSocket = new Socket(serverHost, serverPort);
            // open command
            PrintWriter controlWriter = new PrintWriter(controlSocket.getOutputStream(), true);
            controlWriter.println(String.format("LIST %s %s", userName, password));
            BufferedReader controlReader = new BufferedReader(new InputStreamReader(controlSocket.getInputStream()));
            String psMessage;
            while ((psMessage = controlReader.readLine())!=null) {
                System.out.println(psMessage);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                controlSocket.close();
            } catch (Exception ignored) {}
        }
    }

    public static void main(String[] args) {
        if (args.length != 3) {
            System.err.println("Usage: PunchClientListCommand <server_host:server_port> <user_name> <password>");
            System.exit(1);
        }
        String serverHost = args[0].split(":")[0];
        int serverPort = Integer.parseInt(args[0].split(":")[1]);
        String userName = args[1];
        String password = args[2];
        PunchClientListCommand client = new PunchClientListCommand(serverHost, serverPort, userName, password);
        client.start();
    }
}
