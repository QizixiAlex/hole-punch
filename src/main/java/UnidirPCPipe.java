import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;

/**
 * Worker thread for connecting two sockets unidirectionally on Punch Client Side
 * **/
public class UnidirPCPipe implements Runnable {

    private Socket fromSocket;
    private Socket toSocket;
    private final static int BUF_SIZE = 512;

    public UnidirPCPipe(Socket fromSocket, Socket toSocket) {
        this.fromSocket = fromSocket;
        this.toSocket = toSocket;
    }

    @Override
    public void run() {
        try {
            InputStreamReader in = new InputStreamReader(fromSocket.getInputStream());
            OutputStreamWriter out = new OutputStreamWriter(toSocket.getOutputStream());
            char[] buf = new char[BUF_SIZE];
            int bytes = 0;
            while ((bytes = in.read(buf, 0, BUF_SIZE)) > 0) {
                out.write(buf, 0, bytes);
                out.flush();
            }
        } catch (Exception ignored) {}
    }
}
