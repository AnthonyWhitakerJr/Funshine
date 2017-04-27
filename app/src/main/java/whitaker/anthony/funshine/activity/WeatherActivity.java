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
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

import whitaker.anthony.funshine.R;
import whitaker.anthony.funshine.model.DailyWeatherReport;

public class WeatherActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private static final String LOG_TAG = WeatherActivity.class.getSimpleName();
    public static final int PERMISSION_LOCATION_REQUEST_CODE = 111;
    private static final String URL_BASE ="HTTP://api.openweathermap.org/data/2.5/forecast";
    private static final String URL_PREFIX_LAT = "?lat=";
    private static final String URL_PREFIX_LONG = "&lon=";
    private static final String URL_API_KEY = "&APPID=e336da790e585d214604883c92e1731c";
    private static final String URL_UNITS = "&units=imperial";

    private GoogleApiClient googleApiClient;
    private ArrayList<DailyWeatherReport> reports = new ArrayList<>();

    private ImageView weatherIcon;
    private ImageView weatherIconMini;
    private TextView weatherDate;
    private TextView currentTemp;
    private TextView highTemp;
    private TextView lowTemp;
    private TextView cityCountry;
    private TextView weatherDescription;

    WeatherAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);

        weatherIcon = (ImageView)findViewById(R.id.weatherIcon);
        weatherIconMini = (ImageView)findViewById(R.id.weatherIconMini);
        weatherDate = (TextView)findViewById(R.id.weatherDate);
        currentTemp = (TextView)findViewById(R.id.currentTemp);
        lowTemp = (TextView)findViewById(R.id.lowTemp);
        highTemp = (TextView)findViewById(R.id.highTemp);
        cityCountry = (TextView)findViewById(R.id.cityCountry);
        weatherDescription = (TextView)findViewById(R.id.weatherDescription);

        RecyclerView recyclerView = (RecyclerView)findViewById(R.id.contentWeatherReports);

        adapter = new WeatherAdapter(reports);

        recyclerView.setAdapter(adapter);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);

        recyclerView.setLayoutManager(layoutManager);

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
                try {
                    JSONObject city = response.getJSONObject("city");
                    String cityName = city.getString("name");
                    String country = city.getString("country");

                    JSONArray list = response.getJSONArray("list");
                    for(int i=0; i<5; i++) {
                        JSONObject object = list.getJSONObject(i);
                        DailyWeatherReport report = DailyWeatherReport.newInstance(cityName, country, object);
                        reports.add(report);
                    }

                    Log.v("JSON", "Name: " + cityName + " - Country: " + country);
                } catch (JSONException e) {
                    Log.e(LOG_TAG, "Error:" + e.getLocalizedMessage());
                }

                updateUI();
                adapter.notifyDataSetChanged();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(LOG_TAG, "Error:" + error.getLocalizedMessage());
            }
        });

        Volley.newRequestQueue(this).add(jsonRequest);
    }

    public void updateUI() {
        if(!reports.isEmpty()) {
            DailyWeatherReport report = reports.get(0);
            weatherIcon.setImageDrawable(getResources().getDrawable(report.getWeather().getIconId()));
            weatherIconMini.setImageDrawable(getResources().getDrawable(report.getWeather().getMiniIconId()));

            DateFormat format = new SimpleDateFormat("MMM, dd", Locale.getDefault());
            weatherDate.setText("Today, " + format.format(report.getDate()));

            currentTemp.setText(String.format(Locale.ENGLISH, "%d", report.getCurrentTemp()));
            highTemp.setText(String.format(Locale.ENGLISH, "%d", report.getMaxTemp()));
            lowTemp.setText(String.format(Locale.ENGLISH, "%d", report.getMinTemp()));
            cityCountry.setText(report.getCityName() + ", " + report.getCountry());
            weatherDescription.setText(report.getWeather().getText());
        }
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

    public class WeatherAdapter extends RecyclerView.Adapter<WeatherReportViewHolder> {

        private ArrayList<DailyWeatherReport> reports;

        public WeatherAdapter(ArrayList<DailyWeatherReport> reports) {
            this.reports = reports;
        }

        @Override
        public WeatherReportViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View card = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_weather, parent, false);
            return new WeatherReportViewHolder(card);
        }

        @Override
        public void onBindViewHolder(WeatherReportViewHolder holder, int position) {
            DailyWeatherReport report = reports.get(position);
            holder.updateUI(report);
        }

        @Override
        public int getItemCount() {
            return reports.size();
        }
    }

    public class WeatherReportViewHolder extends RecyclerView.ViewHolder {

        private ImageView icon;
        private TextView date;
        private TextView description;
        private TextView tempHigh;
        private TextView tempLow;

        public WeatherReportViewHolder(View itemView) {
            super(itemView);

            icon = (ImageView)itemView.findViewById(R.id.cardIcon);
            date = (TextView)itemView.findViewById(R.id.cardDay);
            description = (TextView)itemView.findViewById(R.id.cardDescription);
            tempHigh = (TextView)itemView.findViewById(R.id.cardTempHigh);
            tempLow = (TextView)itemView.findViewById(R.id.cardTempLow);
        }

        public void updateUI(DailyWeatherReport report) {
            icon.setImageDrawable(getResources().getDrawable(report.getWeather().getMiniIconId()));

            DateFormat format = new SimpleDateFormat("EEEE", Locale.getDefault());
            date.setText(format.format(report.getDate()));

            tempHigh.setText(String.format(Locale.ENGLISH, "%d", report.getMaxTemp()));
            tempLow.setText(String.format(Locale.ENGLISH, "%d", report.getMinTemp()));
            description.setText(report.getWeather().getText());
        }
    }
}
