package whitaker.anthony.funshine.activity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import org.json.JSONObject;

import whitaker.anthony.funshine.R;

public class WeatherActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private static final String LOG_TAG = WeatherActivity.class.getSimpleName();
    public static final int PERMISSION_LOCATION_REQUEST_CODE = 111;
    private static final String URL_BASE ="HTTP://api.openweathermap.org/data/2.5/forecast";
    private static final String URL_PREFIX_LAT = "?lat=";
    private static final String URL_PREFIX_LONG = "&lon=";
    private static final String URL_API_KEY = "&APPID=e336da790e585d214604883c92e1731c";
    private static final String URL_UNITS = "&units=imperial";

    private GoogleApiClient googleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);

        googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .enableAutoManage(this, this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
        .build();
    }

    private void downloadWeatherData(Location location) {
//        final String url = getUrlForCoordinates(9.968782, 76.299076);
        final String url = getUrlForCoordinates(location.getLatitude(), location.getLongitude());
        Log.v(LOG_TAG, "Url: " + url);

        final JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.v(LOG_TAG, "Response: " + response.toString());
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(LOG_TAG, "Error:" + error.getLocalizedMessage());
            }
        });

        Volley.newRequestQueue(this).add(jsonRequest);
    }

    static String getUrlForCoordinates(double latitude, double longitude) {
        return URL_BASE + URL_PREFIX_LAT + latitude + URL_PREFIX_LONG + longitude + URL_UNITS + URL_API_KEY;
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_LOCATION_REQUEST_CODE);
            Log.v(LOG_TAG, "Requesting permissions.");
        } else {
            Log.v(LOG_TAG, "Permissions already granted.");
            startLocationServices();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        downloadWeatherData(location);
    }

    public void startLocationServices() {
        Log.v(LOG_TAG, "Starting location services.");

        try {
            LocationRequest request = LocationRequest.create().setPriority(LocationRequest.PRIORITY_LOW_POWER);
            LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, request, this);
            Log.v(LOG_TAG, "Location updates requested.");
        } catch (SecurityException exception) {
            Toast.makeText(this, "No permission = app no workie. You broked da codez!", Toast.LENGTH_SHORT).show();
            Log.v(LOG_TAG, exception.toString());
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case PERMISSION_LOCATION_REQUEST_CODE:
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.v(LOG_TAG, "Permissions granted. Starting services.");
                    startLocationServices();
                } else {
                    Log.v(LOG_TAG, "Permission not granted.");
                    Toast.makeText(this, "No permission = app no workie. You broked da codez!", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }
}
