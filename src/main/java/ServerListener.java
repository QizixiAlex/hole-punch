import com.google.common.util.concurrent.SimpleTimeLimiter;
import com.google.common.util.concurrent.TimeLimiter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ServerListener implements Runnable{

    private int listenPort;
    private ConnectionInfo info;
    private SecurityUtils security;
    private PrintWriter controlWriter;
    private static final ExecutorService executor = Executors.newFixedThreadPool(4);

    public ServerListener(int listenPort, ConnectionInfo info, PrintWriter controlWriter) {
        this.listenPort = listenPort;
        this.info = info;
        this.controlWriter = controlWriter;
        this.security = new SecurityUtils();
    }

    @Override
    public void run() {
        ServerSocket outServerSocket = null;
        ServerSocket pcServerSocket = null;
        try {
            outServerSocket = new ServerSocket(listenPort);
            pcServerSocket = new ServerSocket(0);
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
            while (true) {
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
            info.open = false;
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
