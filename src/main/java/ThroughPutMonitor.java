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
        this.seconds = seconds;
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
        prevInput += inputData;
        prevOutput += outputData;
        inputData = inputData*8 / (1024*1024*seconds); //MBit per second
        outputData = outputData*8 / (1024*1024*seconds); //MBit per second
        System.out.println(String.format("input throughput: %.4f mega bits output throughput: %.4f mega bits", inputData, outputData));
    }

}
