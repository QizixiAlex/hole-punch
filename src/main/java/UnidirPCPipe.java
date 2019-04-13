import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

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
            InputStream in = fromSocket.getInputStream();
            OutputStream out = toSocket.getOutputStream();
            byte[] buf = new byte[BUF_SIZE];
            int bytes = 0;
            while (true) {
                while (in.available() != 0) {
                    bytes = in.read(buf);
                    out.write(buf, 0, bytes);
                }
            }
        } catch (Exception ignored) {}
    }
}
