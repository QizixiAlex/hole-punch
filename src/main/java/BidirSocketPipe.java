import java.net.Socket;

public class BidirSocketPipe implements Runnable{

    private Socket fromSocket;
    private Socket toSocket;


    public BidirSocketPipe(Socket fromSocket, Socket toSocket) {
        this.fromSocket = fromSocket;
        this.toSocket = toSocket;
    }


    //  from socket <---> to socket
    @Override
    public void run() {
        byte[] leftBuf =
    }
}
