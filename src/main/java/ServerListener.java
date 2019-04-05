import com.google.common.util.concurrent.SimpleTimeLimiter;
import com.google.common.util.concurrent.TimeLimiter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.Charset;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ServerListener implements Runnable{

    private static final int NONCE_LENGTH = 256;
    private int listenPort;
    private ConnectionInfo info;
    private PrintWriter controlWriter;
    private static final ExecutorService executor = Executors.newFixedThreadPool(4);

    private String generateNonce() {
        byte[] array = new byte[NONCE_LENGTH];
        new Random().nextBytes(array);
        return new String(array, Charset.forName("UTF-8"));
    }

    public ServerListener(int listenPort, ConnectionInfo info, PrintWriter controlWriter) {
        this.listenPort = listenPort;
        this.info = info;
        this.controlWriter = controlWriter;
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
            String nonce = generateNonce();
            controlWriter.write(String.format("CONNECT %d %S", pcServerSocket.getLocalPort(), nonce));
            TimeLimiter limiter = SimpleTimeLimiter.create(executor);
            final ServerSocket finalPcServerSocket = pcServerSocket;
            Socket pcSocket = limiter.callWithTimeout(new Callable<Socket>() {
                @Override
                public Socket call() throws Exception {
                    return finalPcServerSocket.accept();
                }
            }, 10, TimeUnit.SECONDS);
            //check nonce
            BufferedReader pcReader = new BufferedReader(new InputStreamReader(pcSocket.getInputStream()));
            String pcNonce = pcReader.readLine();
            if (!pcNonce.equals(nonce)) {
                //close pcSocket
                pcSocket.close();
                throw new Exception("nonce no match");
            }
            info.open = true;
            info.clientIP = pcSocket.getRemoteSocketAddress().toString();
            while (true) {
                Socket outSocket = outServerSocket.accept();
                UnidirectionalSocketPipe leftPipe = new UnidirectionalSocketPipe(outSocket, pcSocket, info, "OUT");
                leftPipe.run();
                UnidirectionalSocketPipe rightPipe = new UnidirectionalSocketPipe(outSocket, pcSocket, info, "IN");
                rightPipe.run();
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
