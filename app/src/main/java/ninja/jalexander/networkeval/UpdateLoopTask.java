package ninja.jalexander.networkeval;

import android.app.Activity;
import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.view.View;
import android.widget.TextView;

/**
 * Created by jalex on 2/22/2018.
 */

public class UpdateLoopTask extends AsyncTask<Void, Void, NetworkData> {
    private Activity parent;
    UpdateListener listener;
    private Context context;

    public UpdateLoopTask(Activity p, UpdateListener l, Context c){
        parent = p;
        listener = l;
        context = c;
    }

    @Override
    protected NetworkData doInBackground(Void... params) {
        NetworkData data = new NetworkData();

        updateWifi(data);

        return data;
    }

    private void updateWifi(NetworkData data){
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        data.wifiFrequency = wifiInfo.getFrequency();
    }

    @Override
    protected void onPostExecute(NetworkData data) {
        if (listener != null) {
            listener.updateFields(data);
        }
    }
}
