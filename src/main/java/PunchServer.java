import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class PunchServer {

    private int portNum;
    private List<ConnectionInfo> connectionInfoList;

    PunchServer(int portNum) {
        this.portNum = portNum;
        connectionInfoList = new ArrayList<ConnectionInfo>();
    }

    public void run() {
        //create socket to listen for client requests
        //todo: set authenticator file path
        Authenticator authenticator = new Authenticator("");
        try {
            ServerSocket serverSocket = new ServerSocket(portNum);
            while (true) {
                Socket controlSocket = serverSocket.accept();
                //read pc request
                BufferedReader controlReader = new BufferedReader(new InputStreamReader(controlSocket.getInputStream()));
                String pcRequest = controlReader.readLine();
                String[] tokens = pcRequest.split("\\s+");
                String requestType = tokens[0];
                //two types of request
                if (requestType.equals("OPEN")) {
                    //open request
                    String userName = tokens[1];
                    String password = tokens[2];
                    int  listenPort = Integer.parseInt(tokens[3]);
                    if (!authenticator.authenticate(userName, password, listenPort)) {
                        //authentication failure, send delayed response to pc
                        DelayResponse delayResponse = new DelayResponse(controlSocket, 30);
                        delayResponse.run();
                        controlSocket.close();
                        return;
                    }
                    //authenticated
                    PrintWriter controlWriter = new PrintWriter(controlSocket.getOutputStream(), true);
                    controlWriter.write(String.format("CONNECTED %d", listenPort));
                    //setup connection info
                    ConnectionInfo currentConnectionInfo = new ConnectionInfo(userName, listenPort);
                    connectionInfoList.add(currentConnectionInfo);
                    ServerListener listener = new ServerListener(listenPort, currentConnectionInfo, controlWriter);
                    listener.run();
                } else if (requestType.equals("LIST")){
                    for (ConnectionInfo info : connectionInfoList) {
                        if (info.open) {
                            info.display();
                        }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Usage: PunchServer <port number>");
            System.exit(1);
        }
        int portNum = Integer.parseInt(args[0]);
        PunchServer server = new PunchServer(portNum);
        server.run();
    }
}
