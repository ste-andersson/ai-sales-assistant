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


    public static String promptOpenAi(String instruction, String prompt) throws IOException {

        String apiKey = Dotenv.load().get("OPENAI_API_KEY");
        JsonNode config = yamlMapper.readTree(Main.class.getResourceAsStream("/config.yaml"));

        List<TextToTextOpenAIOutgoing.Message> messages = List.of(
                new TextToTextOpenAIOutgoing.Message("system", instruction),
                new TextToTextOpenAIOutgoing.Message("user", prompt)
        );

        TextToTextOpenAIOutgoing outgoingRequest = new TextToTextOpenAIOutgoing(config.get("TextToText").get("Model").asText(), messages);

        String jsonPayload = jsonMapper.writeValueAsString(outgoingRequest);

        Request request = new Request.Builder()
                .url("https://api.openai.com/v1/chat/completions")
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .post(RequestBody.create(jsonPayload, MediaType.parse("application/json")))
                .build();

            performanceLog.info("START: Call to ChatGPT");

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("OpenAI svarade med felkod: " + response.code() + " - " + response.message());
            }

            String responseBody = response.body().string();
            TextToTextOpenAiIncoming mappedResponse = jsonMapper.readValue(responseBody, TextToTextOpenAiIncoming.class);
            performanceLog.info("STOP: Text from ChatGPT");
            return mappedResponse.choices().get(0).message().content();
        }
    }
}