import java.io.*;
import java.net.Socket;

/**
 * Worker thread for connecting two sockets unidirectionally on the Punch Server side
 * **/
public class UnidirPSPipe implements Runnable {

    private Socket outSocket;
    private Socket pcSocket;
    private ConnectionInfo info;
    private String direction; // if outward, then the pipe data flows from pc to outside client

    private static final int BUF_SIZE = 512;

    public UnidirPSPipe(Socket outSocket, Socket pcSocket, ConnectionInfo info, String direction) {
        this.outSocket = outSocket;
        this.pcSocket = pcSocket;
        this.info = info;
        this.direction = direction;
    }

    @Override
    public void run() {
        InputStreamReader in = null;
        OutputStreamWriter out = null;
        try {
            if (direction.equals("OUT")) {
                in = new InputStreamReader(pcSocket.getInputStream());
                out = new OutputStreamWriter(outSocket.getOutputStream());
            } else {
                in = new InputStreamReader(outSocket.getInputStream());
                out = new OutputStreamWriter(pcSocket.getOutputStream());
            }
        } catch (IOException ignored){}
        char[] buf = new char[BUF_SIZE];
        int bytesCount = 0;
        try {
              while ((bytesCount = in.read(buf, 0, BUF_SIZE)) > 0) {
                  out.write(buf, 0, bytesCount);
                  out.flush();
                  if (direction.equals("OUT")) {
                      info.outTraffic += bytesCount;
                  } else {
                      info.inTraffic += bytesCount;
                  }
              }
        } catch (Exception ignored){
            //ignore
        }
    }
}
