public class PunchClient {

    private int localPort;
    private int serverPort;
    private int remotePort;
    private String serverHost;
    private String userName;
    private String password;

    public PunchClient(int localPort, int serverPort, int remotePort, String serverHost, String userName, String password) {
        this.localPort = localPort;
        this.serverPort = serverPort;
        this.remotePort = remotePort;
        this.serverHost = serverHost;
        this.userName = userName;
        this.password = password;
    }

    public void start() {

    }

    public static void main(String[] args) {
        if (args.length != 5) {
            System.err.println("Usage: PunchClient <local_port> <server_host:server_port> <user_name> <password> <remote_port>");
            System.exit(1);
        }
        int localPort = Integer.parseInt(args[0]);
        String serverHost = args[1].split(":")[0];
        int serverPort = Integer.parseInt(args[1].split(":")[1]);
        String userName = args[2];
        String password = args[3];
        int remotePort = Integer.parseInt(args[4]);
        PunchClient client = new PunchClient(localPort, serverPort, remotePort, serverHost, userName, password);
        client.start();
    }
}
