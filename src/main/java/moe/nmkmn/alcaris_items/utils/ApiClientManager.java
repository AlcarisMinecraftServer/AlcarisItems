package moe.nmkmn.alcaris_items.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class ApiClientManager {
    private final String apiUrl;

    public ApiClientManager(String apiUrl) {
        this.apiUrl = apiUrl;
    }

    public String fetchItemData() throws IOException {
        HttpURLConnection connection;
        URL url = new URL(apiUrl + "/items");
        connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Accept", "application/json");

        if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
            throw new IOException("HTTP error code: " + connection.getResponseCode() + " - " + connection.getResponseMessage());
        }

        try (InputStream inputStream = connection.getInputStream();
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            StringBuilder responseBuilder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                responseBuilder.append(line);
            }
            return responseBuilder.toString();
        } finally {
            connection.disconnect();
        }
    }
}
