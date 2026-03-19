package se.sveki.aiapiplayground;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.cdimascio.dotenv.Dotenv;
import okhttp3.*;
import se.sveki.aiapiplayground.models.TextToTextOpenAIOutgoing;
import se.sveki.aiapiplayground.models.TextToTextOpenAiIncoming;

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

            String responseBody = response.body().string();
            TextToTextOpenAiIncoming mappedResponse = mapper.readValue(responseBody, TextToTextOpenAiIncoming.class);
            return mappedResponse.choices().get(0).message().content();
        }
    }
}