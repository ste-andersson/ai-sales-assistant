package se.sveki.aiapiplayground;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import io.github.cdimascio.dotenv.Dotenv;
import okhttp3.*;
import javazoom.jl.player.Player; // JLayer
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.sveki.aiapiplayground.models.TextToSpeechElevenLabsOutgoing;

import java.io.*;

public class TextToSpeech {
    private static final OkHttpClient client = new OkHttpClient();
    private static final ObjectMapper jsonMapper = new ObjectMapper();
    private static final YAMLMapper yamlMapper = new YAMLMapper();
    private static final Logger performanceLog = LoggerFactory.getLogger("PerformanceLogger");

    public void speak(String script) throws Exception {

        JsonNode config = yamlMapper.readTree(Main.class.getResourceAsStream("/config.yaml"));

        if(config.get("FunctionToggle").get("TextToSpeech").asText().equals("Off")){
            return;
        }

        String apiKey = Dotenv.load().get("ELEVEN_LABS_KEY");


        TextToSpeechElevenLabsOutgoing.VoiceSettings settings =
                new TextToSpeechElevenLabsOutgoing.VoiceSettings(0.5, 0.8);

        TextToSpeechElevenLabsOutgoing outgoingRequest = new TextToSpeechElevenLabsOutgoing(
                script,
                config.get("TextToSpeech").get("Model").asText(),
                settings
        );

        String jsonPayload = jsonMapper.writeValueAsString(outgoingRequest);

        String url = "https://api.elevenlabs.io/v1/text-to-speech/" + config.get("TextToSpeech").get("VoiceId").asText() +
                "?optimize_streaming_latency=" + config.get("TextToSpeech").get("OptimizeStreamingLatency").asText();

        Request request = new Request.Builder()
                .url(url)
                .header("xi-api-key", apiKey)
                .post(RequestBody.create(jsonPayload, MediaType.parse("application/json")))
                .build();

        System.out.println("Anropar ElevenLabs...");
        performanceLog.info("START: Call to ElevenLabs");

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                System.err.println("Fel från ElevenLabs: " + response.code());
                return;
            }

            performanceLog.info("STOP: Response from ElevenLabs");
            System.out.println("Ljud mottaget. Startar uppspelning...");

            try (InputStream inputStream = new BufferedInputStream(response.body().byteStream())) {
                Player player = new Player(inputStream);
                player.play();
            }

            System.out.println("Uppspelning klar!");
        }
    }
}