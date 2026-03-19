package se.sveki.aiapiplayground;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.cdimascio.dotenv.Dotenv;
import okhttp3.*;
import se.sveki.aiapiplayground.models.TextToTextOpenAIOutgoing;

import java.io.IOException;
import java.util.List;

public class TextToText {

    private static final OkHttpClient client = new OkHttpClient();
    private static final ObjectMapper mapper = new ObjectMapper();

    public static String promptOpenAi(String model, String instruction, String prompt) throws IOException {

        String apiKey = Dotenv.load().get("OPENAI_API_KEY");

        List<TextToTextOpenAIOutgoing.Message> messages = List.of(
                new TextToTextOpenAIOutgoing.Message("system", instruction),
                new TextToTextOpenAIOutgoing.Message("user", prompt)
        );

        TextToTextOpenAIOutgoing outgoingRequest = new TextToTextOpenAIOutgoing(model, messages);

        String jsonPayload = mapper.writeValueAsString(outgoingRequest);

        Request request = new Request.Builder()
                .url("https://api.openai.com/v1/chat/completions")
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .post(RequestBody.create(jsonPayload, MediaType.parse("application/json")))
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("OpenAI svarade med felkod: " + response.code() + " - " + response.message());
            }

            // response.body().string() kan bara läsas EN gång, så vi sparar den i en variabel
            String responseBody = response.body().string();
            return getResponseContent(responseBody);
        }
    }

    private static String getResponseContent(String responseJson) {
        try {
            JsonNode root = mapper.readTree(responseJson);
            return root
                    .path("choices")
                    .get(0)
                    .path("message")
                    .path("content")
                    .asText();
        } catch (Exception e) {
            throw new RuntimeException("Could not parse OpenAI response", e);
        }
    }
}
