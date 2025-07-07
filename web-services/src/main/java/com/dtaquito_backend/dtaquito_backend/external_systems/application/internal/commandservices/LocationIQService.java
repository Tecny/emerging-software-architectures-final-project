package com.dtaquito_backend.dtaquito_backend.external_systems.application.internal.commandservices;

import com.dtaquito_backend.dtaquito_backend.external_systems.domain.model.entities.LocationIQResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.cdimascio.dotenv.Dotenv;
import lombok.extern.slf4j.Slf4j;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Slf4j
@Service
public class LocationIQService {

    private static final Dotenv dotenv = Dotenv.load();

    private String apiKey = dotenv.get("LOCATION_IQ_KEY");

    private final OkHttpClient client = new OkHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();

    public LocationIQResponse reverseGeocode(double lat, double lon) {
        HttpUrl url = HttpUrl.parse("https://us1.locationiq.com/v1/reverse.php")
                .newBuilder()
                .addQueryParameter("key", apiKey)
                .addQueryParameter("lat", String.valueOf(lat))
                .addQueryParameter("lon", String.valueOf(lon))
                .addQueryParameter("accept-language", "es")
                .addQueryParameter("normalizeaddress", "1")
                .addQueryParameter("format", "json")
                .build();

        Request request = new Request.Builder()
                .url(url)
                .get()
                .addHeader("Accept", "application/json")
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String errorBody = response.body() != null
                        ? response.body().string()
                        : "<empty>";
                throw new IllegalArgumentException(
                        "Unable to reverse geocode - HTTP " +
                                response.code() + " " + errorBody);
            }

            String json = response.body().string();
            // Reverse devuelve un único objeto, no un array
            LocationIQResponse result = mapper.readValue(json, LocationIQResponse.class);
            log.info("Reverse geocode raw JSON: {}", json);
            return result;

        } catch (IOException e) {
            throw new IllegalArgumentException("Unable to reverse geocode - I/O error", e);
        }
    }
}