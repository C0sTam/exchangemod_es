package com.costam.exchangebot.client.util;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.client.MinecraftClient;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class ApiClient {

    private static final String BASE_API_URL = "http://";
    private final HttpClient httpClient;
    private final MinecraftClient client;
    private final String version = "1.0.19";

    public ApiClient(MinecraftClient client) {
        this.httpClient = HttpClient.newHttpClient();
        this.client = client;
    }

    public void checkAccessAndVersion() {
        try {
            if (!checkVersion(version)) {
                client.scheduleStop();
            }
        } catch (Exception e) {
            e.printStackTrace();
            client.scheduleStop();
        }
    }


    private boolean checkVersion(String version) throws Exception {
        String versionCheckUrl = BASE_API_URL + "version/check?version=" + version;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(versionCheckUrl))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 200) {
            JsonObject jsonResponse = JsonParser.parseString(response.body()).getAsJsonObject();
            return jsonResponse.get("valid").getAsBoolean();
        }

        return false;
    }
}