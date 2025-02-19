package moe.nmkmn.alcaris_items.utils;

import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class ApiClientManager {
    private final String apiUrl;
    private final String apiKey;

    public ApiClientManager(String apiUrl, String apiKey) {
        this.apiUrl = apiUrl;
        this.apiKey = apiKey;
    }

    public String fetchItemData() throws IOException {
        HttpURLConnection connection = getConnection();

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

    private @NotNull HttpURLConnection getConnection() throws IOException {
        HttpURLConnection connection;
        URL url = new URL(apiUrl + "/items");
        connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Accept", "application/json");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Authorization", "Basic " + apiKey);

        if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
            throw new IOException("HTTP error code: " + connection.getResponseCode() + " - " + connection.getResponseMessage());
        }
        return connection;
    }
}
