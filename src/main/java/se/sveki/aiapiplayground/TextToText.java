package se.sveki.aiapiplayground;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import io.github.cdimascio.dotenv.Dotenv;
import okhttp3.*;
import se.sveki.aiapiplayground.models.TextToTextOpenAIOutgoing;
import se.sveki.aiapiplayground.models.TextToTextOpenAiIncoming;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

public class TextToText {

    private static final OkHttpClient client = new OkHttpClient();
    private static final ObjectMapper jsonMapper = new ObjectMapper();
    private static final YAMLMapper yamlMapper = new YAMLMapper();
    private static final Logger performanceLog = LoggerFactory.getLogger("PerformanceLogger");

    public static String textToText(String instruction, String prompt) throws IOException {

        JsonNode config = yamlMapper.readTree(Main.class.getResourceAsStream("/config.yaml"));
        JsonNode tttConfig = config.get("TextToText");
        String provider = tttConfig.get("Provider").asText();
        String model = tttConfig.get("Model").asText();

        String url;
        String apiKey;

        switch (provider.toUpperCase()) {
            case "GROQ" -> {
                url = "https://api.groq.com/openai/v1/chat/completions";
                apiKey = Dotenv.load().get("GROQ_KEY");
            }
            case "OPENAI" -> {
                url = "https://api.openai.com/v1/chat/completions";
                apiKey = Dotenv.load().get("OPENAI_API_KEY");
            }
            default -> throw new IllegalArgumentException("Oväntad TTT Provider: " + provider);
        }

        List<TextToTextOpenAIOutgoing.Message> messages = List.of(
                new TextToTextOpenAIOutgoing.Message("system", instruction),
                new TextToTextOpenAIOutgoing.Message("user", prompt)
        );

        TextToTextOpenAIOutgoing outgoingRequest = new TextToTextOpenAIOutgoing(model, messages);
        String jsonPayload = jsonMapper.writeValueAsString(outgoingRequest);

        Request request = new Request.Builder()
                .url(url)
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .post(RequestBody.create(jsonPayload, MediaType.parse("application/json")))
                .build();

            performanceLog.info("START: Call to " + provider + "(" + model + ")");

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                String errorJson = response.body() != null ? response.body().string() : "Empty body";
                throw new IOException(provider + " Error " + response.code() + ": " + errorJson);
            }

            String responseBody = response.body().string();
            TextToTextOpenAiIncoming mappedResponse = jsonMapper.readValue(responseBody, TextToTextOpenAiIncoming.class);
            performanceLog.info("STOP: Text from " + provider + "(" + model + ")");
            return mappedResponse.choices().get(0).message().content();
        }
    }
}