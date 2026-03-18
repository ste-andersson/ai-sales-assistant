package se.sveki.aiapiplayground;

import io.github.cdimascio.dotenv.Dotenv;
import okhttp3.*;
import java.io.*;
import javax.sound.sampled.*;
import java.util.Scanner;

public class SpeechToText {
    public static String speechToText() throws Exception {
        String apiKey = Dotenv.load().get("OPENAI_API_KEY");
        File fil = new File("speech.wav");

        // 1. SPELA IN (Starta med Enter, stoppa med Enter)
        System.out.println("Tryck ENTER för att spela in...");
        new Scanner(System.in).nextLine();

        AudioFormat format = new AudioFormat(16000, 16, 1, true, false);
        TargetDataLine line = AudioSystem.getTargetDataLine(format);
        line.open(format);
        line.start();

        // En tråd som skriver ljudet till filen i bakgrunden
        Thread t = new Thread(() -> {
            try { AudioSystem.write(new AudioInputStream(line), AudioFileFormat.Type.WAVE, fil); }
            catch (Exception e) {}
        });
        t.start();

        System.out.println("Spelar in... Tryck ENTER för att stoppa.");
        new Scanner(System.in).nextLine();
        line.stop();
        line.close();

        // 2. SKICKA (Whisper API)
        System.out.println("Transkriberar...");
        OkHttpClient client = new OkHttpClient();

        RequestBody body = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("model", "whisper-1")
                .addFormDataPart("file", fil.getName(), RequestBody.create(fil, MediaType.parse("audio/wav")))
                .build();

        Request req = new Request.Builder()
                .url("https://api.openai.com/v1/audio/transcriptions")
                .header("Authorization", "Bearer " + apiKey)
                .post(body).build();

        try (Response res = client.newCall(req).execute()) {
            String filteredResponse = filteredResponse(res.body().string());
            return filteredResponse;
        }
    }

    private static String filteredResponse(String unfilteredResponse) {
        int start = unfilteredResponse.indexOf("\"text\":\"") + 8;
        int slut = unfilteredResponse.indexOf("\"", start);
        return unfilteredResponse.substring(start, slut);
    }
}
