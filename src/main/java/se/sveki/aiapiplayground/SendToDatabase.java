package se.sveki.aiapiplayground;

import okhttp3.*;
import java.io.IOException;

public class SendToDatabase {

    private static final OkHttpClient client = new OkHttpClient();

    public static void postCrmEntry(String inputText, String URL) {
        RequestBody body = RequestBody.create(
                inputText,
                MediaType.get("application/json; charset=utf-8")
        );

        Request request = new Request.Builder()
                .url(URL)
                .post(body)
                .build();

        // Vi kör anropet asynkront eller i en try-with-resources för att stänga responsen
        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful()) {
                System.out.println("Databas-push lyckades: " + response.code());
            } else {
                System.err.println("Databas-push misslyckades: " + response.code() + " - " + response.body().string());
            }
        } catch (IOException e) {
            System.err.println("Kunde inte ansluta till Spring Boot-servern: " + e.getMessage());
        }
    }

}
