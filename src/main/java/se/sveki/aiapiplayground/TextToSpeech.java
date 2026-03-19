package se.sveki.aiapiplayground;

import io.github.cdimascio.dotenv.Dotenv;
import okhttp3.*;
import javazoom.jl.player.Player; // JLayer
import java.io.*;

public class TextToSpeech {
    public static void elevenLabsVoice(String voiceId, String script) throws Exception {
        String apiKey = Dotenv.load().get("ELEVEN_LABS_KEY");

        String url = "https://api.elevenlabs.io/v1/text-to-speech/" + voiceId;

        String json = """
        {
          "text": "%s",
          "model_id": "eleven_multilingual_v2"
        }
        """;

        json = String.format(json, script);

        OkHttpClient client = new OkHttpClient();
        RequestBody body = RequestBody.create(json, MediaType.parse("application/json"));
        Request request = new Request.Builder()
                .url(url)
                .header("xi-api-key", apiKey)
                .post(body)
                .build();

        System.out.println("Anropar ElevenLabs...");

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                System.out.println("Ljud mottaget. Startar uppspelning...");

                // Vi använder en BufferedInputStream för stabilare uppspelning
                InputStream is = new BufferedInputStream(response.body().byteStream());

                // JLayer spelaren
                Player player = new Player(is);

                // .play() blockerar tråden tills ljudet är slut
                player.play();

                System.out.println("Uppspelning klar!");
            } else {
                System.err.println("Fel: " + response.code() + " - " + response.body().string());
            }
        }
    }
}