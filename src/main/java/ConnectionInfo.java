public class ConnectionInfo {

    public int inTraffic;
    public int outTraffic;
    public int outPortNum;
    public int pcPortNum;   // need to be set later
    public String clientIP; // need to be set later
    public String userName;
    public boolean open;

    public ConnectionInfo(String userName, int outPortNum) {
        inTraffic = 0;
        outTraffic = 0;
        open = false;
        this.userName = userName;
        this.outPortNum = outPortNum;
    }

    public void display() {
        System.out.println(String.format("%s %d %d %s IN:%d Bytes OUT:%d Bytes",
                userName, outPortNum, pcPortNum, clientIP, inTraffic, outTraffic));
    }
}
