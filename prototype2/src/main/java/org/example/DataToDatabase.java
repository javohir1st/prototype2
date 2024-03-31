package org.example;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class DataToDatabase {
    private static final String API_KEY = "a31e7a106635faa6152c41a052f1ab1b";
    private static final String DB_URL = "jdbc:postgresql://localhost:5432/postgres";
    private static final String USER = "postgres";
    private static final String PASSWORD = "root";

    private static final String CITY = "Slough"; // Default city

    // Method to fetch weather data from OpenWeatherMap API
    private static JSONObject fetchWeatherData(String city) throws IOException {
        String apiUrl = "http://api.openweathermap.org/data/2.5/weather?q=" + city + "&appid=" + API_KEY + "&units=metric"; // Request temperature in Celsius
        URL url = new URL(apiUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            StringBuilder response = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                response.append(line);
            }

            return new JSONObject(response.toString());
        }
    }

    // Method to insert weather data into PostgreSQL database
    private static void insertWeatherData(JSONObject weatherData) throws SQLException {
        String city = weatherData.optString("name");
        JSONObject main = weatherData.optJSONObject("main");
        if (city.isEmpty() || main == null) {
            throw new JSONException("Invalid JSON data");
        }

        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASSWORD)) {
            String sql = "INSERT INTO weatherApp(city, temp, humidity) VALUES (?, ?, ?)";
            try (PreparedStatement preparedStatement = conn.prepareStatement(sql)) {
                preparedStatement.setString(1, city);
                preparedStatement.setDouble(2, main.optDouble("temp"));
                preparedStatement.setDouble(3, main.optDouble("humidity"));
                preparedStatement.executeUpdate();
            }
        }
    }

    public static void main(String[] args) {
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

        Runnable task = () -> {
            try {
                JSONObject weatherData = fetchWeatherData(CITY);
                insertWeatherData(weatherData);
                System.out.println("Weather data for " + CITY + " inserted successfully!");
            } catch (IOException | SQLException | JSONException e) {
                e.printStackTrace();
            }
        };

        // Schedule the task to run every hour
        scheduler.scheduleAtFixedRate(task, 0, 1, TimeUnit.HOURS);
    }
}
