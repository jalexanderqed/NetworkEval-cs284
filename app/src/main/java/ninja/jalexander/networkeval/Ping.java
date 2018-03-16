package ninja.jalexander.networkeval;

import android.util.Log;

import java.io.*;
import java.net.*;

public class Ping {
    private final static String serverIP = "13.57.60.32";
    private final static int serverPort = 5678;

    public static double getPing() throws Exception {
        try (DatagramSocket clientSocket = new DatagramSocket()) {
            Log.d("STATUS", "Running getPing");
            InetAddress IPAddress = InetAddress.getByName(serverIP);
            byte[] sendData = new byte[2];
            byte[] receiveData = new byte[2];
            String a = "a";
            sendData = a.getBytes();
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, serverPort);
            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);

            clientSocket.send(sendPacket);

            clientSocket.setSoTimeout(10000);
            long start_time = System.nanoTime();
            clientSocket.receive(receivePacket);
            long end_time = System.nanoTime();
            long RTTNanos = end_time - start_time;
            double RTTms = (double) RTTNanos / 1000000.0;
            clientSocket.close();
            return RTTms;
        }
    }
}
