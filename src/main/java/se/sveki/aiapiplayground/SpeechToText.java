package se.sveki.aiapiplayground;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import io.github.cdimascio.dotenv.Dotenv;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.sveki.aiapiplayground.models.SpeechToTextOpenAiIncoming;

import java.io.*;
import javax.sound.sampled.*;
import java.util.Scanner;

public class SpeechToText {
    private static final OkHttpClient client = new OkHttpClient();
    private static final ObjectMapper jsonMapper = new ObjectMapper();
    private static final YAMLMapper yamlMapper = new YAMLMapper();
    private static final Logger performanceLog = LoggerFactory.getLogger("PerformanceLogger");

    public String listen(String conversationStatus) throws IOException, LineUnavailableException {

        JsonNode config = yamlMapper.readTree(Main.class.getResourceAsStream("/config.yaml"));
        JsonNode sttConfig = config.get("SpeechToText");
        String provider = sttConfig.get("Provider").asText();
        String model = sttConfig.get("Model").asText();
        JsonNode mock = yamlMapper.readTree(Main.class.getResourceAsStream("/mock.yaml"));

        if (config.get("FunctionToggle").get("SpeechToText").asText().equals("Off")) {
            return mock.get("SpeechToText").get(conversationStatus).asText();
        }

        String url;
        String apiKey;

        switch (provider.toUpperCase()) {
            case "GROQ" -> {
                url = "https://api.groq.com/openai/v1/audio/transcriptions";
                apiKey = Dotenv.load().get("GROQ_KEY");
            }
            case "OPENAI" -> {
                url = "https://api.openai.com/v1/audio/transcriptions";
                apiKey = Dotenv.load().get("OPENAI_API_KEY");
            }
            default -> throw new IllegalArgumentException("Oväntad STT Provider: " + provider);
        }

        File file = new File("speech.wav");

        System.out.println("Tryck ENTER för att spela in...");
        new Scanner(System.in).nextLine();

        AudioFormat format = new AudioFormat(16000, 16, 1, true, false);
        TargetDataLine line = AudioSystem.getTargetDataLine(format);
        line.open(format);
        line.start();

        Thread recordingThread = new Thread(() -> {
            try {
                AudioSystem.write(new AudioInputStream(line), AudioFileFormat.Type.WAVE, file);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        recordingThread.start();

        System.out.println("Spelar in... Tryck ENTER för att stoppa.");
        new Scanner(System.in).nextLine();
        line.stop();
        line.close();

        System.out.println("Transkriberar...");

        RequestBody body = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("model", model)
                .addFormDataPart("file", file.getName(), RequestBody.create(file, MediaType.parse("audio/wav")))
                .build();

        Request req = new Request.Builder()
                .url(url)
                .header("Authorization", "Bearer " + apiKey)
                .post(body).build();

        performanceLog.info("START: Call to {} ({})", provider, model);

        try (Response res = client.newCall(req).execute()) {
            if (!res.isSuccessful()) throw new IOException(provider + " error: " + res.code());

            SpeechToTextOpenAiIncoming result = jsonMapper.readValue(res.body().string(), SpeechToTextOpenAiIncoming.class);
            performanceLog.info("STOP: Response from {} ({})", provider, model);
            return result.text();
        }
    }
}
