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
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements UpdateListener {

    boolean locationStarted = false;
    final int MY_PERMISSION_ACCESS_LOCATION = 0x2313;
    UpdateLoopTask updateLoop;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
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

        final Activity me = this;
        final UpdateListener alsoMe = this;
        Timer myTimer = new Timer();
        myTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                updateLoop = new UpdateLoopTask(me, alsoMe, getMainActivity().getApplicationContext());
                updateLoop.execute();
            }

        }, 0, 1000);
    }

    public void updateFields(NetworkData data){
        TextView freqField = (TextView)findViewById(R.id.freq);
        freqField.setText(Integer.toString(data.wifiFrequency));
    }

    public void startLocation() {
        locationStarted = true;
        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        LocationListener locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                // Called when a new location is found by the location provider.
                TextView latField = (TextView)findViewById(R.id.lat);
                TextView lonField = (TextView)findViewById(R.id.lon);
                latField.setText(Double.toString(location.getLatitude()));
                lonField.setText(Double.toString(location.getLongitude()));
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
