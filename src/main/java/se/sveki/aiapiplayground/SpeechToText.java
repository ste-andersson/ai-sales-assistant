package se.sveki.aiapiplayground;

import com.fasterxml.jackson.databind.ObjectMapper;
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
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final Logger performanceLog = LoggerFactory.getLogger("PerformanceLogger");

    public static String speechToText() throws Exception {

        String apiKey = Dotenv.load().get("OPENAI_API_KEY");
        File file = new File("speech.wav");

        System.out.println("Tryck ENTER för att spela in...");
        new Scanner(System.in).nextLine();

        AudioFormat format = new AudioFormat(16000, 16, 1, true, false);
        TargetDataLine line = AudioSystem.getTargetDataLine(format);
        line.open(format);
        line.start();

        Thread t = new Thread(() -> {
            try {
                AudioSystem.write(new AudioInputStream(line), AudioFileFormat.Type.WAVE, file);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        t.start();

        System.out.println("Spelar in... Tryck ENTER för att stoppa.");
        new Scanner(System.in).nextLine();
        line.stop();
        line.close();

        System.out.println("Transkriberar...");

        RequestBody body = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("model", "whisper-1")
                .addFormDataPart("file", file.getName(), RequestBody.create(file, MediaType.parse("audio/wav")))
                .build();

        Request req = new Request.Builder()
                .url("https://api.openai.com/v1/audio/transcriptions")
                .header("Authorization", "Bearer " + apiKey)
                .post(body).build();

        performanceLog.info("START: Call to Whisper");

        try (Response res = client.newCall(req).execute()) {
            if (!res.isSuccessful()) throw new IOException("Whisper error: " + res.code());

            SpeechToTextOpenAiIncoming result = mapper.readValue(res.body().string(), SpeechToTextOpenAiIncoming.class);
            performanceLog.info("STOP: Response from Whisper");
            return result.text();
        }
    }
}
