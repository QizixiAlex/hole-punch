import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class BidirSocketPipe implements Runnable{

    private Socket fromSocket;
    private Socket toSocket;
    private final int BUF_SIZE = 512;

    public BidirSocketPipe(Socket fromSocket, Socket toSocket) {
        this.fromSocket = fromSocket;
        this.toSocket = toSocket;
    }

    //  from socket <---> to socket
    @SuppressWarnings("Duplicates")
    @Override
    public void run() {
        try {
            InputStream fromIn = fromSocket.getInputStream();
            InputStream toIn = toSocket.getInputStream();
            OutputStream fromOut = fromSocket.getOutputStream();
            OutputStream toOut = toSocket.getOutputStream();
            byte[] leftBuf = new byte[BUF_SIZE];
            byte[] rightBuf = new byte[BUF_SIZE];
            while (true) {
                int leftBytes = toIn.read(leftBuf);
                if (leftBytes < 0) {
                    //connection closed
                    throw new Exception();
                } else {
                    System.out.println(String.format("left traffic: %d", leftBytes));
                    fromOut.write(leftBuf, 0, leftBytes);
                }
                int rightBytes = fromIn.read(rightBuf);
                if (rightBytes < 0) {
                    //connection closed
                    throw new Exception();
                } else {
                    System.out.println(String.format("right traffic: %d", rightBytes));
                    toOut.write(rightBuf, 0, rightBytes);
                }
            }
        } catch (Exception ignored) {
        } finally {
            try {
                fromSocket.close();
                toSocket.close();
            } catch (IOException ignored) {}
        }
    }
}
