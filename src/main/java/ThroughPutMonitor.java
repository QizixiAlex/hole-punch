import java.util.List;
import java.util.TimerTask;

public class ThroughPutMonitor extends TimerTask {

    private List<ConnectionInfo> serverStatus;
    private double prevInput;
    private double prevOutput;
    private int seconds; // time period between each run

    public ThroughPutMonitor(List<ConnectionInfo> serverStatus, int seconds) {
        this.serverStatus = serverStatus;
        prevInput = 0;
        prevOutput = 0;
    }

    @Override
    public void run() {
        double currentInput = 0;
        double currentOutput = 0;
        for (ConnectionInfo connectionInfo : serverStatus) {
            currentInput += connectionInfo.inTraffic;
            currentOutput += connectionInfo.outTraffic;
        }
        double inputData = currentInput - prevInput;
        double outputData = currentOutput - prevOutput;
        inputData = inputData / (1024*1024*seconds); //MB per second
        outputData = outputData / (1024*1024*seconds); //MB per second
        System.out.println(String.format("input throughput: %.2f output throughput: %.2f", inputData, outputData));
    }

}
