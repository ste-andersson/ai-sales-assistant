package se.sveki.aiapiplayground;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.cdimascio.dotenv.Dotenv;
import okhttp3.*;

import java.io.IOException;

public class TextToText {

    public static String promptOpenAi(String model, String instruction, String prompt) throws IOException {

        String apiKey = Dotenv.load().get("OPENAI_API_KEY");

        String jsonPayload = """
            {
              "model": "gpt-4o-mini",
              "messages": [
                {"role": "system", "content": "%s"},
                {"role": "user", "content": "%s"}
              ]
            }
            """;

        jsonPayload = String.format(jsonPayload, instruction, prompt);

        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder()
                .url("https://api.openai.com/v1/chat/completions")
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .post(RequestBody.create(jsonPayload, MediaType.parse("application/json")))
                .build();

        try (Response response = client.newCall(request).execute()) {
            String filteredResponse = getResponseContent(response.body().string());

            return filteredResponse;
        }
    }

    private static String getResponseContent(String responseJson) {
        ObjectMapper mapper = new ObjectMapper();

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
