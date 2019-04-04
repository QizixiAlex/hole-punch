import java.net.Socket;

public class BidirectionalSocketPipe implements Runnable {

    private Socket pcSocket;
    private Socket outSocket;
    private ConnectionInfo info;

    public BidirectionalSocketPipe(Socket pcSocket, Socket outSocket, ConnectionInfo info) {
        this.pcSocket = pcSocket;
        this.outSocket = outSocket;
        this.info = info;
    }

    @Override
    public void run() {

    }

}
