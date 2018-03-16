package ninja.jalexander.networkeval;

import android.util.Log;

import java.io.*;
import java.net.*;

class UDPClient {
    private final static String serverIP = "13.57.60.32";
    private final static int serverPort = 9876;
    private static final int retryCount = 1;

    public static void runDiagnostic(NetworkData netData, boolean isWifi) throws Exception {
        int packetsReceived = 0;
        long nanoDiff = 0;
        double bRate = 0;
        double pRate = 0;
        int bytesReceived = 0;
        byte[] finishBytes = "messiii".getBytes();

        InetAddress IPAddress = InetAddress.getByName(serverIP);
        String sentence = "Gimme dem kitties";
        int waitTime = 15;
        double receivedPerc = 0.0;
        double threshold = 0.8;

        double ping = Ping.getPing();

        while (receivedPerc < threshold) {
            try (DatagramSocket clientSocket = new DatagramSocket()) {
                waitTime = waitTime + 5;
                String waitString = "" + waitTime;
                byte[] sendData = waitString.getBytes();
                for (int i = 0; i < retryCount; i++) {
                    DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, serverPort);
                    clientSocket.send(sendPacket);
                }

                boolean receiving = true;
                packetsReceived = 0;
                bytesReceived = 0;

                boolean started = false;
                long startTime = 0;
                long endTime = 0;

                clientSocket.setSoTimeout(20000);

                while (receiving) {
                    byte[] receiveData = new byte[64000];

                    if (!started) {
                        started = true;
                        startTime = System.nanoTime();
                    }

                    DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
                    try {
                        clientSocket.receive(receivePacket);
                    }
                    catch(SocketTimeoutException e){
                        Log.e("ERROR", "UDP timed out");
                        endTime = System.nanoTime();
                        packetsReceived = 0;
                        receiving = false;
                        break;
                    }

                    packetsReceived++;
                    bytesReceived += receivePacket.getLength();

                    if (receivePacket.getLength() == finishBytes.length) {
                        byte[] received = receivePacket.getData();
                        if (Util.bytesEqual(received, finishBytes, finishBytes.length)) {
                            receiving = false;
                            endTime = System.nanoTime();
                        }
                    }
                }

                nanoDiff = endTime - startTime;
                bRate = ((double) bytesReceived) / (nanoDiff * 1e-9);
                pRate = ((double) packetsReceived) / (nanoDiff * 1e-9);
                bRate /= 1e6;

                receivedPerc = packetsReceived / 101.0; //hardcoded packet num

                Log.d("DEBUG", "Received: " + receivedPerc);
                if (receivedPerc < threshold) {
                    Util.wait(2000);
                }
            }
        }
        Log.d("DEBUG", "Final wait time: " + waitTime);
        Log.d("DEBUG", "Final received percentage: " + receivedPerc);

        if (isWifi) {
            netData.wifiUdpDeltaTime = nanoDiff;
            netData.wifiUdpPacketsReceived = packetsReceived;
            netData.wifiUdpPacketRate = pRate;
            netData.wifiUdpBytesReceived = bytesReceived;
            netData.wifiUdpByteRate = bRate;
            netData.wifiPing = ping;
        } else {
            netData.dataUdpDeltaTime = nanoDiff;
            netData.dataUdpPacketsReceived = packetsReceived;
            netData.dataUdpPacketRate = pRate;
            netData.dataUdpBytesReceived = bytesReceived;
            netData.dataUdpByteRate = bRate;
            netData.dataPing = ping;
        }
    }
}
