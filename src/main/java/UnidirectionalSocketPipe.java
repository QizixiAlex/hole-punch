import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class UnidirectionalSocketPipe implements Runnable {

    private Socket outSocket;
    private Socket pcSocket;
    private ConnectionInfo info;
    private String direction; // if outward, then the pipe data flows from pc to outside client

    private static final int BUF_SIZE = 512;

    public UnidirectionalSocketPipe(Socket outSocket, Socket pcSocket, ConnectionInfo info, String direction) {
        this.outSocket = outSocket;
        this.pcSocket = pcSocket;
        this.info = info;
        this.direction = direction;
    }

    @Override
    public void run() {
        InputStream in = null;
        OutputStream out = null;
        try {
            if (direction.equals("OUT")) {
                in = pcSocket.getInputStream();
                out = outSocket.getOutputStream();
            } else {
                in = outSocket.getInputStream();
                out = pcSocket.getOutputStream();
            }
        } catch (IOException ignored){}
        byte[] buf = new byte[BUF_SIZE];
        int bytesCount = 0;
        try {
            while ((bytesCount = in.read(buf)) != -1) {
                out.write(buf, 0, bytesCount);
                if (direction.equals("OUT")) {
                    info.outTraffic += bytesCount;
                } else {
                    info.inTraffic += bytesCount;
                }
            }
        } catch (Exception ignored){
            //ignore
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException ignored) {}
            }
            if (out != null) {
                try {
                    out.close();
                } catch (IOException ignored) {}
            }
        }
    }
}
