import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;

/**
 * Punch Server Program as in Homework Doc
 * **/
public class PunchServer {

    private int portNum;
    private List<ConnectionInfo> connectionInfoList;

    PunchServer(int portNum) {
        this.portNum = portNum;
        connectionInfoList = new ArrayList<>();
    }

    // security feature: ensure request is not malformed
    private boolean validRequest(String request) {
        if (request.startsWith("OPEN")) {
            String[] openTokens = request.split("\\s+");
            //check argument
            if (openTokens.length != 4) {
                return false;
            }
            //check portnumber
            try {
                int portNum = Integer.parseInt(openTokens[3]);
                if (portNum < 0 || portNum >= 65536) {
                    return false;
                }
            } catch (NumberFormatException e) {
                return false;
            }
            return true;
        } else if (request.startsWith("LIST")) {
            String[] listTokens = request.split("\\s+");
            if (listTokens.length != 3) {
                return false;
            }
            return true;
        } else {
            return false;
        }
    }

    public void run() {
        //create socket to listen for client requests
        Authenticator authenticator = new Authenticator(System.getenv("HOME")+"/.hole_punch/users");
        try {
            ServerSocket serverSocket = new ServerSocket(portNum);
            System.out.println(String.format("Punch Server Listening on port: %d", portNum));
            //monitor traffic
            Timer timer = new Timer();
            timer.schedule(new ThroughPutMonitor(connectionInfoList,10), 100000, 10000);
            while (true) {
                Socket controlSocket = serverSocket.accept();
                //read pc request
                BufferedReader controlReader = new BufferedReader(new InputStreamReader(controlSocket.getInputStream()));
                String pcRequest = controlReader.readLine();
                if (pcRequest == null || pcRequest.length() == 0 || !validRequest(pcRequest)) {
                    continue;
                }
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
                        Thread delayResponseThread = new Thread(new DelayResponse(controlSocket, 30));
                        delayResponseThread.start();
                        continue;
                    }
                    //authenticated
                    PrintWriter controlWriter = new PrintWriter(controlSocket.getOutputStream(), true);
                    controlWriter.println(String.format("CONNECTED %d", listenPort));
                    //setup connection info
                    ServerListener listener = new ServerListener(listenPort, connectionInfoList, controlWriter, userName);
                    Thread listenerThread = new Thread(listener);
                    listenerThread.setDaemon(true);
                    listenerThread.start();
                } else if (requestType.equals("LIST")){
                    String userName = tokens[1];
                    String password = tokens[2];
                    if (!authenticator.authenticate(userName, password)) {
                        //authentication failure, send delayed response to pc
                        Thread delayResponseThread = new Thread(new DelayResponse(controlSocket, 30));
                        delayResponseThread.start();
                        continue;
                    }
                    PrintWriter controlWriter = new PrintWriter(controlSocket.getOutputStream(), true);
                    controlWriter.println(String.format("CONNECTIONINFO %d", connectionInfoList.size()));
                    for (ConnectionInfo info : connectionInfoList) {
                        if (info.open) {
                            controlWriter.println(info.display());
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
