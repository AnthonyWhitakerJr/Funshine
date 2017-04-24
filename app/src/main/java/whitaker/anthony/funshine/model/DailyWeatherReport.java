package whitaker.anthony.funshine.model;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * @author Anthony Whitaker.
 */
public class DailyWeatherReport {

    private static final String LOG_TAG = DailyWeatherReport.class.getSimpleName();

    public enum WeatherType {
        CLOUDS("Clouds"), CLEAR("Clear"), RAIN("Rain"), WIND("Wind"), SNOW("Snow");

        private String text;

        WeatherType(String text) {
            this.text = text;
        }

        public static WeatherType fromString(String text) {
            for (WeatherType weatherType : WeatherType.values()) {
                if (weatherType.text.equalsIgnoreCase(text)) {
                    return weatherType;
                }
            }
            return null;
        }
    }

    private String cityName;
    private String country;
    private int currentTemp;
    private Date date;
    private int maxTemp;
    private int minTemp;
    private WeatherType weather;

    public DailyWeatherReport(String cityName, String country, int currentTemp, String dateString, int maxTemp, int minTemp, String weather) {
        this.cityName = cityName;
        this.country = country;
        this.currentTemp = currentTemp;
        this.date = parseDate(dateString);
        this.maxTemp = maxTemp;
        this.minTemp = minTemp;
        this.weather = WeatherType.fromString(weather);
    }

    public static DailyWeatherReport newInstance(String cityName, String country, JSONObject object) {
        try {
            JSONObject main = object.getJSONObject("main");
            Double currentTemp = main.getDouble("temp");
            Double maxTemp = main.getDouble("temp_max");
            Double minTemp = main.getDouble("temp_min");

            JSONArray weatherList = object.getJSONArray("weather");
            JSONObject weather = weatherList.getJSONObject(0);
            String weatherType = weather.getString("main");

            String rawDate = object.getString("dt_txt");

            return new DailyWeatherReport(cityName, country,
                    currentTemp.intValue(), rawDate,
                    maxTemp.intValue(), minTemp.intValue(),
                    weatherType);
        } catch (JSONException e) {
            Log.e(LOG_TAG, "Error:" + e.getLocalizedMessage());
        }

        return null;
    }

    private static Date parseDate(String dateString) {
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
        try {
            return format.parse(dateString);
        } catch (ParseException e) {
            return null;
        }
    }

    public String getCityName() {
        return cityName;
    }

    public String getCountry() {
        return country;
    }

    public int getCurrentTemp() {
        return currentTemp;
    }

    public Date getDate() {
        return date;
    }

    public int getMaxTemp() {
        return maxTemp;
    }

    public int getMinTemp() {
        return minTemp;
    }

    public WeatherType getWeather() {
        return weather;
    }
}
