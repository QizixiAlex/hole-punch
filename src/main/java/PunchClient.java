import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class PunchClient {

    private int localPort;
    private int serverPort;
    private int remotePort;
    private String serverHost;
    private String userName;
    private String password;

    public PunchClient(int localPort, int serverPort, int remotePort, String serverHost, String userName, String password) {
        this.localPort = localPort;
        this.serverPort = serverPort;
        this.remotePort = remotePort;
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
            controlWriter.write(String.format("OPEN %s %s %d", userName, password, remotePort));
            BufferedReader controlReader = new BufferedReader(new InputStreamReader(controlSocket.getInputStream()));
            String psMessage;
            while ((psMessage = controlReader.readLine())!=null) {
                if (psMessage.startsWith("FAIL")) {
                    // connection failed
                    controlSocket.close();
                    return;
                } else if (psMessage.startsWith("CONNECTED")) {
                    continue;
                } else if (psMessage.startsWith("CONNECT")) {
                    //connect with ps listening thread
                    int psThreadPort = Integer.parseInt(psMessage.split("\\s+")[1]);
                    String nonce = psMessage.split("\\s+")[2];
                    Socket psSocket = new Socket(serverHost, psThreadPort);
                    PrintWriter psWriter = new PrintWriter(psSocket.getOutputStream(), true);
                    psWriter.write(nonce);
                    //connect sockets
                    ServerSocket localListener = new ServerSocket(localPort);
                    Socket localSocket = localListener.accept();
                    BidirSocketPipe bdsp = new BidirSocketPipe(psSocket, localSocket);
                    bdsp.run();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        if (args.length != 5) {
            System.err.println("Usage: PunchClient <local_port> <server_host:server_port> <user_name> <password> <remote_port>");
            System.exit(1);
        }
        int localPort = Integer.parseInt(args[0]);
        String serverHost = args[1].split(":")[0];
        int serverPort = Integer.parseInt(args[1].split(":")[1]);
        String userName = args[2];
        String password = args[3];
        int remotePort = Integer.parseInt(args[4]);
        PunchClient client = new PunchClient(localPort, serverPort, remotePort, serverHost, userName, password);
        client.start();
    }
}
