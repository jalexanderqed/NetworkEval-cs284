package ninja.jalexander.networkeval;

import android.app.Activity;
import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.telephony.SignalStrength;
import android.util.Log;

import java.text.DateFormat;
import java.time.Clock;
import java.time.LocalTime;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by jalex on 2/22/2018.
 */

public class UpdateNetDataTask extends AsyncTask<Void, Void, NetworkData> {
    private Activity parent;
    private UpdateListener listener;
    private Context context;
    private DataListener dataListener;

    public UpdateNetDataTask(Activity p, UpdateListener l, Context c, DataListener dl) {
        parent = p;
        listener = l;
        context = c;
        dataListener = dl;
    }

    @Override
    protected NetworkData doInBackground(Void... params) {
        NetworkData data = new NetworkData();
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);

        updateWifi(data, wifiManager);
        Log.d("STATUS", "Updated wi-fi");

        while (!wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(true);
            Util.wait(100);
        }
        Log.d("STATUS", "Enabled wifi");

        if (dataListener.lastStrength != null) {
            updateData(data);
            Log.d("STATUS", "Updated data");
        }

        Log.d("STATUS", "Started updating TCP");
        TCPClient.runDiagnostic(data, true);
        Log.d("STATUS", "Finished updating TCP");

        Log.d("STATUS", "Started updating UDP");
        UDPClient.runDiagnostic(data, true);
        Log.d("STATUS", "Finished updating UDP");

        while (wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(false);
            Util.wait(100);
        }
        Log.d("STATUS", "Disabled wifi");

        Log.d("STATUS", "Started updating TCP");
        TCPClient.runDiagnostic(data, false);
        Log.d("STATUS", "Finished updating TCP");

        Log.d("STATUS", "Started updating UDP");
        UDPClient.runDiagnostic(data, false);
        Log.d("STATUS", "Finished updating UDP");

        wifiManager.setWifiEnabled(true);

        DateFormat df = DateFormat.getTimeInstance();
        df.setTimeZone(TimeZone.getTimeZone("America/Los_Angeles"));
        String gmtTime = df.format(new Date());

        String resString = gmtTime + "," +
                MainActivity.lon + "," +
                MainActivity.lat + "," +
                data.wifiFrequency + "," +
                data.wifiLinkSpeed + "," +
                data.wifiRssi + "," +
                data.wifiSignalLevel + "," +
                data.dataSignal + "," +
                data.dataIsGsm + "," +
                data.dataGsmStrength + "," +
                data.dataCdmaDbm + "," +
                data.dataEvdoDbm + "," +
                data.dataLteDbm + "," +
                data.wifiTcpDeltaTime + "," +
                data.wifiTcpBytesReceived + "," +
                data.wifiTcpByteRate + "," +
                data.wifiUdpDeltaTime + "," +
                data.wifiUdpPacketsReceived + "," +
                data.wifiUdpPacketRate + "," +
                data.wifiUdpBytesReceived + "," +
                data.wifiUdpByteRate + "," +
                data.dataTcpDeltaTime + "," +
                data.dataTcpBytesReceived + "," +
                data.dataTcpByteRate + "," +
                data.dataUdpDeltaTime + "," +
                data.dataUdpPacketsReceived + "," +
                data.dataUdpPacketRate + "," +
                data.dataUdpBytesReceived + "," +
                data.dataUdpByteRate;

        DataPoster.post(resString);

        return data;
    }

    private void updateWifi(NetworkData data, WifiManager wifiManager) {
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        data.wifiFrequency = wifiInfo.getFrequency();
        data.wifiLinkSpeed = wifiInfo.getLinkSpeed();
        data.wifiRssi = wifiInfo.getRssi();
        data.wifiSignalLevel = wifiManager.calculateSignalLevel(data.wifiRssi, 100);
    }

    private void updateData(NetworkData data) {
        data.dataSignal = dataListener.lastStrength.getLevel();
        data.dataIsGsm = dataListener.lastStrength.isGsm();
        data.dataGsmStrength = dataListener.lastStrength.getGsmSignalStrength();
        data.dataCdmaDbm = dataListener.lastStrength.getCdmaDbm();
        data.dataEvdoDbm = dataListener.lastStrength.getEvdoDbm();
        data.dataLteDbm = getLteStrength(dataListener.lastStrength);
    }

    private int getLteStrength(SignalStrength strength) {
        String signals = strength.toString();
        String[] parts = signals.split(" ");
        return Integer.parseInt(parts[9]);
    }

    @Override
    protected void onPostExecute(NetworkData data) {
        if (listener != null) {
            listener.updateFields(data);
        }
    }
}
