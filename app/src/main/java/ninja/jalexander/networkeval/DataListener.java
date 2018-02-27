package ninja.jalexander.networkeval;

import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;

/**
 * Created by jalex on 2/27/2018.
 */

public class DataListener extends PhoneStateListener {
    public SignalStrength lastStrength = null;
    long lastUpdated = 0;

    @Override
    public void onSignalStrengthsChanged(SignalStrength signalStrength) {
        lastStrength = signalStrength;
        lastUpdated = System.currentTimeMillis();
    }
}
