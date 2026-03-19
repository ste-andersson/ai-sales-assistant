package se.sveki.aiapiplayground;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.cdimascio.dotenv.Dotenv;
import okhttp3.*;
import javazoom.jl.player.Player; // JLayer
import se.sveki.aiapiplayground.models.TextToSpeechElevenLabsOutgoing;

import java.io.*;

public class TextToSpeech {
    private static final OkHttpClient client = new OkHttpClient();
    private static final ObjectMapper mapper = new ObjectMapper();

    public static void elevenLabsVoice(String voiceId, String script) throws Exception {

        String apiKey = Dotenv.load().get("ELEVEN_LABS_KEY");

        TextToSpeechElevenLabsOutgoing.VoiceSettings settings =
                new TextToSpeechElevenLabsOutgoing.VoiceSettings(0.5, 0.8);

        TextToSpeechElevenLabsOutgoing outgoingRequest = new TextToSpeechElevenLabsOutgoing(
                script,
                "eleven_turbo_v2_5",
                settings
        );

        String jsonPayload = mapper.writeValueAsString(outgoingRequest);

        String url = "https://api.elevenlabs.io/v1/text-to-speech/" + voiceId +
                "?optimize_streaming_latency=2";

        Request request = new Request.Builder()
                .url(url)
                .header("xi-api-key", apiKey)
                .post(RequestBody.create(jsonPayload, MediaType.parse("application/json")))
                .build();

        System.out.println("Anropar ElevenLabs...");

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                System.err.println("Fel från ElevenLabs: " + response.code());
                return;
            }

            System.out.println("Ljud mottaget. Startar uppspelning...");

            try (InputStream inputStream = new BufferedInputStream(response.body().byteStream())) {
                Player player = new Player(inputStream);
                player.play();
            }

            System.out.println("Uppspelning klar!");
        }
    }
}