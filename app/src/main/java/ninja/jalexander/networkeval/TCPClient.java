package ninja.jalexander.networkeval;

import java.io.*;
import java.net.*;

class TCPClient {
    private final static String serverIP = "13.57.60.32";
    private final static int serverPort = 6789;

    public static void runDiagnostic(NetworkData netData, boolean isWifi) throws Exception {
        byte[] inBuff = new byte[64000];
        int bytesRead;
        int bytesReceived = 0;

        boolean started = false;
        long startTime = 0;
        long endTime = 0;

        try (
                Socket clientSocket = new Socket(serverIP, serverPort);
                InputStream in = clientSocket.getInputStream();
        ) {
            do {
                if (!started) {
                    started = true;
                    startTime = System.nanoTime();
                }

                bytesRead = in.read(inBuff, 0, inBuff.length);
                bytesReceived += bytesRead;
            } while (bytesRead != -1);
        }

        if (started) endTime = System.nanoTime();

        long nanoDiff = endTime - startTime;
        double bRate = ((double) bytesReceived) / (nanoDiff * 1e-9);
        bRate /= 1e6;

        if(isWifi) {
            netData.wifiTcpDeltaTime = nanoDiff;
            netData.wifiTcpBytesReceived = bytesReceived;
            netData.wifiTcpByteRate = bRate;
        }
        else{
            netData.dataTcpDeltaTime = nanoDiff;
            netData.dataTcpBytesReceived = bytesReceived;
            netData.dataTcpByteRate = bRate;
        }
    }
}
