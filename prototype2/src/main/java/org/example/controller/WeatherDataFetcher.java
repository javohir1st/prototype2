package org.example.controller;

import org.json.JSONArray;
import org.json.JSONObject;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class WeatherDataFetcher {
    private static final String DB_URL = "jdbc:postgresql://localhost:5432/postgres";
    private static final String USER = "postgres";
    private static final String PASSWORD = "root";





    public static JSONArray getWeatherDataAsJson() throws SQLException {
        JSONArray jsonArray = new JSONArray();
        try (Connection conn = DriverManager.getConnection(DB_URL, USER, PASSWORD)) {
            String sql = "SELECT * FROM weatherapp ORDER BY id DESC LIMIT 1";
            PreparedStatement preparedStatement = conn.prepareStatement(sql);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("city", resultSet.getString("city"));
                jsonObject.put("temp", resultSet.getDouble("temp"));
                jsonObject.put("humidity", resultSet.getDouble("humidity"));
                jsonArray.put(jsonObject);
            }
        }
        return jsonArray;
    }








    public static void main(String[] args) {
        try {
            JSONArray weatherData = WeatherDataFetcher.getWeatherDataAsJson();
            System.out.println(weatherData);
        } catch (SQLException e) {
            System.err.println("Error retrieving weather data: " + e.getMessage());
        }
    }




}
