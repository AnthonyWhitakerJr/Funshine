package whitaker.anthony.funshine.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import whitaker.anthony.funshine.R;

public class WeatherActivity extends AppCompatActivity {

    private static final String LOG_TAG = WeatherActivity.class.getSimpleName();
    private static final String URL_BASE ="HTTP://api.openweathermap.org/data/2.5/forecast";
    private static final String URL_PREFIX_LAT = "?lat=";
    private static final String URL_PREFIX_LONG = "&lon=";
    private static final String URL_API_KEY = "&APPID=e336da790e585d214604883c92e1731c";
    private static final String URL_UNITS = "&units=imperial";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);

        final String url = getUrlForCoordinates(9.968782, 76.299076);
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
}
