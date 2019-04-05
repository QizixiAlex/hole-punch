import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

public class DelayResponse implements Runnable {

    private Socket controlSocket;
    private int delaySecs;

    public DelayResponse(Socket controlSocket, int delaySecs) {
        this.controlSocket = controlSocket;
        this.delaySecs = delaySecs;
    }

    @Override
    public void run() {
        try {
            Thread.sleep(delaySecs*1000);
        } catch (InterruptedException ignored) {}
        try {
            PrintWriter controlWriter = new PrintWriter(controlSocket.getOutputStream(), true);
            controlWriter.println("FAIL");
        } catch (IOException ignored) {}
    }
}
