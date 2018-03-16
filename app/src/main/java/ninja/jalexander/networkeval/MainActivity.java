package ninja.jalexander.networkeval;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements UpdateListener {
    public static double lon;
    public static double lat;

    boolean locationStarted = false;
    final int MY_PERMISSION_ACCESS_LOCATION = 0x2313;
    UpdateNetDataTask updateLoop;
    DataListener dataListener;

    boolean status = false;

    final static int FAILED = 0;
    final static int READY = 1;
    final static int WORKING = 2;

    public void updateStatus(int status){
        TextView field = (TextView)findViewById(R.id.status);
        if(status == READY){
            field.setText(R.string.ready);
            field.setTextColor(getColor(R.color.ready));
        }
        else if(status == FAILED){
            field.setText(R.string.failed);
            field.setTextColor(getColor(R.color.unready));
        }
        else if(status == WORKING){
            field.setText(R.string.working);
            field.setTextColor(getColor(R.color.working));
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final Activity me = this;
        final UpdateListener alsoMe = this;

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateLoop = new UpdateNetDataTask(me, alsoMe, getMainActivity().getApplicationContext(), dataListener);
                updateLoop.execute();
                Snackbar.make(view, "Started update task", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                updateStatus(WORKING);
            }
        });

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION, android.Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSION_ACCESS_LOCATION);
        }
        else{
            if(!locationStarted) {
                startLocation();
            }
        }

        dataListener = new DataListener();
        final TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        telephonyManager.listen(dataListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
    }

    public void updateFields(NetworkData data){
        if(data == null){
            Snackbar.make(findViewById(R.id.lat), "Update task FAILED", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show();
            updateStatus(FAILED);
            return;
        }
        updateStatus(READY);
        ((TextView)findViewById(R.id.wifiFreq)).setText(Integer.toString(data.wifiFrequency));
        ((TextView)findViewById(R.id.wifiLinkSpeed)).setText(Integer.toString(data.wifiLinkSpeed));
        ((TextView)findViewById(R.id.wifiRssi)).setText(Integer.toString(data.wifiRssi));
        ((TextView)findViewById(R.id.wifiSignal)).setText(Integer.toString(data.wifiSignalLevel));

        ((TextView)findViewById(R.id.dataSignal)).setText(Integer.toString(data.dataSignal));
        ((TextView)findViewById(R.id.dataIsGsm)).setText(Boolean.toString(data.dataIsGsm));
        ((TextView)findViewById(R.id.dataGsmStrength)).setText(Integer.toString(data.dataGsmStrength));
        ((TextView)findViewById(R.id.dataCdmaStrength)).setText(Integer.toString(data.dataCdmaDbm));
        ((TextView)findViewById(R.id.dataEvdoStrength)).setText(Integer.toString(data.dataEvdoDbm));
        ((TextView)findViewById(R.id.dataLteStrength)).setText(Integer.toString(data.dataLteDbm));

        ((TextView)findViewById(R.id.wifiTcpByteRate)).setText(Double.toString(Math.round(data.wifiTcpByteRate * 100) / 100.0) + " MBps");

        ((TextView)findViewById(R.id.wifiUdpByteRate)).setText(Double.toString(Math.round(data.wifiUdpByteRate * 100) / 100.0) + " MBps");
        ((TextView)findViewById(R.id.wifiUdpPacketPercent)).setText(Double.toString(Math.round(1000 * data.wifiUdpPacketsReceived / 101.0) / 10.0));
        ((TextView)findViewById(R.id.wifiPing)).setText(Double.toString(data.wifiPing));

        ((TextView)findViewById(R.id.dataTcpByteRate)).setText(Double.toString(Math.round(data.dataTcpByteRate * 100) / 100.0) + " MBps");

        ((TextView)findViewById(R.id.dataUdpByteRate)).setText(Double.toString(Math.round(data.dataUdpByteRate * 100) / 100.0) + " MBps");
        ((TextView)findViewById(R.id.dataUdpPacketPercent)).setText(Double.toString(Math.round(1000 * data.dataUdpPacketsReceived / 101.0) / 10.0));
        ((TextView)findViewById(R.id.dataPing)).setText(Double.toString(data.dataPing));

        Snackbar.make(findViewById(R.id.lat), "Finished update task", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show();
    }

    public void startLocation() {
        locationStarted = true;
        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        LocationListener locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                MainActivity.lon = location.getLatitude();
                MainActivity.lat = location.getLatitude();

                TextView latField = (TextView)findViewById(R.id.lat);
                TextView lonField = (TextView)findViewById(R.id.lon);
                latField.setText(Double.toString(location.getLatitude()));
                lonField.setText(Double.toString(location.getLongitude()));
                locationOn();
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {
                switch (status){
                    case LocationProvider.AVAILABLE:
                        locationOn();
                        break;
                    default:
                        locationOff();
                        break;
                }
            }

            public void onProviderEnabled(String provider) {
                locationOn();
            }

            public void onProviderDisabled(String provider) {
                locationOff();
            }
        };

        try {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
        }catch (SecurityException e){
            Log.e("LOC", "Security exception for location should not occurr");
        }
    }

    private void locationOn(){
        TextView field = (TextView)findViewById(R.id.loc_status);
        field.setText(R.string.ready);
        field.setTextColor(getColor(R.color.ready));
    }

    private void locationOff(){
        TextView field = (TextView)findViewById(R.id.loc_status);
        field.setText(R.string.unready);
        field.setTextColor(getColor(R.color.unready));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSION_ACCESS_LOCATION: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if(!locationStarted){
                        startLocation();
                    }
                }
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private Activity getMainActivity(){
        return this;
    }
}
