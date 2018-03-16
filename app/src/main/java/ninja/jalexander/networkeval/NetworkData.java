package ninja.jalexander.networkeval;

/**
 * Created by jalex on 2/23/2018.
 */

public class NetworkData {
    public int wifiFrequency;
    public int wifiLinkSpeed;
    public int wifiRssi;
    public int wifiSignalLevel;

    public int dataSignal;
    public boolean dataIsGsm;
    public int dataGsmStrength;
    public int dataCdmaDbm;
    public int dataEvdoDbm;
    public int dataLteDbm;

    public long wifiTcpDeltaTime;
    public int wifiTcpBytesReceived;
    public double wifiTcpByteRate;

    public long wifiUdpDeltaTime;
    public int wifiUdpPacketsReceived;
    public double wifiUdpPacketRate;
    public int wifiUdpBytesReceived;
    public double wifiUdpByteRate;
    public double wifiPing;

    public long dataTcpDeltaTime;
    public int dataTcpBytesReceived;
    public double dataTcpByteRate;

    public long dataUdpDeltaTime;
    public int dataUdpPacketsReceived;
    public double dataUdpPacketRate;
    public int dataUdpBytesReceived;
    public double dataUdpByteRate;
    public double dataPing;
}
