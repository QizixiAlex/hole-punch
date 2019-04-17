import com.google.common.util.concurrent.SimpleTimeLimiter;
import com.google.common.util.concurrent.TimeLimiter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Worker thread for Punch Server
 * **/
public class ServerListener implements Runnable{

    private int listenPort;
    private String userName;
    private List<ConnectionInfo> infoList;
    private SecurityUtils security;
    private PrintWriter controlWriter;
    private static final ExecutorService executor = Executors.newFixedThreadPool(10);

    public ServerListener(int listenPort, List<ConnectionInfo> infoList, PrintWriter controlWriter, String userName) {
        this.listenPort = listenPort;
        this.userName = userName;
        this.infoList = infoList;
        this.controlWriter = controlWriter;
        this.security = new SecurityUtils();
    }

    @Override
    public void run() {
        ServerSocket outServerSocket = null;
        ServerSocket pcServerSocket = null;
        List<ConnectionInfo> selfInfoList = new ArrayList<>();
        try {
            outServerSocket = new ServerSocket(listenPort);
            while (true) {
                pcServerSocket = new ServerSocket(0);
                ConnectionInfo info = new ConnectionInfo(userName, listenPort);
                infoList.add(info);
                selfInfoList.add(info);
                info.pcPortNum = pcServerSocket.getLocalPort();
                //handshake with pc
                String nonce = security.generateSalt();
                controlWriter.println(String.format("CONNECT %d %s", pcServerSocket.getLocalPort(), nonce));
                TimeLimiter limiter = SimpleTimeLimiter.create(executor);
                final ServerSocket finalPcServerSocket = pcServerSocket;
                Socket pcSocket = limiter.callWithTimeout(finalPcServerSocket::accept, 10, TimeUnit.SECONDS);
                //check nonce
                BufferedReader pcReader = new BufferedReader(new InputStreamReader(pcSocket.getInputStream()));
                String pcNonce = pcReader.readLine();
                if (!nonce.equals(pcNonce)) {
                    pcSocket.close();
                    throw new Exception("nonce no match");
                }
                info.open = true;
                info.clientIP = pcSocket.getRemoteSocketAddress().toString();
                Socket outSocket = outServerSocket.accept();
                UnidirPSPipe leftPipe = new UnidirPSPipe(outSocket, pcSocket, info, "OUT");
                //leftPipe.run();
                Thread leftThread = new Thread(leftPipe);
                leftThread.setDaemon(true);
                leftThread.start();
                UnidirPSPipe rightPipe = new UnidirPSPipe(outSocket, pcSocket, info, "IN");
                //rightPipe.run();
                Thread rightThread = new Thread(rightPipe);
                rightThread.setDaemon(true);
                rightThread.start();
            }
        } catch (Exception ignored) {
            //do nothing, close sockets in finally
        } finally {
            for (ConnectionInfo info : selfInfoList) {
                info.open = true;
            }
            //close out socket
            if (outServerSocket != null) {
                try {
                    outServerSocket.close();
                } catch (IOException ignored) {}
            }
            //close pc socket
            if (pcServerSocket != null) {
                try {
                    pcServerSocket.close();
                } catch (IOException ignored) {}
            }
        }

    }
}
