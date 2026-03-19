package se.sveki.aiapiplayground;

import okhttp3.*;
import se.sveki.aiapiplayground.utilities.PromptLoader;

import java.time.LocalDate;
import java.util.List;

public class Main {
    public static void main(String[] args) throws Exception {

//        TextToSpeech.elevenLabsVoice("Vo4adEN1y46b0ufuysRe", "Hej! Administratören här! Berätta vad du vill, så ser jag till att all information hamnar rätt. Min lott i livet är ju tyvärr att lyssna på ditt pladder och försöka få någon form av struktur i det...");

        String initialReport = "Jag hade just ett möte med Svante Jonsson på Testingbolaget och de är intresserade av att ta in tre konsulter efter sommaren. Vi har ett nytt möte 3:e juni 15:00 på deras kontor. Påminn mig om att jag behöver kolla med Hanna på torsdag förmiddag om hon är tillgänglig och att jag också måste kolla med Torbjörn i mitten av nästa vecka om han är intresserad av ett underkonsultuppdrag.";
//        try {
//            initialReport = SpeechToText.speechToText();
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }

        System.out.println("\u001B[33mDu: " + initialReport + "\u001B[0m");

        String today = LocalDate.now().toString();

        String initialTextResponse = TextToText.promptOpenAi(
                "gpt-4o-mini",
                PromptLoader.getPrompt("TextToText.OpenAI.Start").replace("{{today}}", LocalDate.now().toString()),
                initialReport);

//        System.out.println(initialTextResponse);

        String comments = LlmResponseProcessor.getResponsePartString(initialTextResponse, "COMMENTS");
        List<String[]> crmData = LlmResponseProcessor.getResponsePartList(initialTextResponse, "CRM");
        List<String[]> remindersData = LlmResponseProcessor.getResponsePartList(initialTextResponse, "REMINDER");

        System.out.println("\n\u001B[36mAssistenten: " + comments + "\u001B[0m");

        System.out.println("\nFörslag för registrering i CRM:");
        LlmResponseProcessor.printListData(crmData, "CRM DATA", "Organisation", "Kontaktperson", "Datum", "Kommentar");

        System.out.println("\nFörslag för påminnelser att registrera:");
        LlmResponseProcessor.printListData(remindersData, "PÅMINNELSE", "Datum", "Tid", "Rubrik", "Detaljer");



        String safeComments = comments.replace("\"", "\\\"");
        TextToSpeech.elevenLabsVoice("Vo4adEN1y46b0ufuysRe", comments);

        String entryDraftReaction = "Det blir jättebra! Jag godkänner det!";

        try {
            entryDraftReaction = SpeechToText.speechToText();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        System.out.println("\u001B[33mDu: " + entryDraftReaction + "\u001B[0m");

        String followUpTextResponse = TextToText.promptOpenAi(
                "gpt-4o-mini",
                PromptLoader.getPrompt("TextToText.OpenAI.FollowUp").replace("{{oldCrm}}", LlmResponseProcessor.getResponsePartString(initialTextResponse, "CRM"))
                        .replace("{{oldReminder}}", LlmResponseProcessor.getResponsePartString(initialTextResponse, "REMINDER")),
                entryDraftReaction
        );

//        System.out.println(followUpTextResponse);
        comments = LlmResponseProcessor.getResponsePartString(followUpTextResponse, "COMMENTS");

        safeComments = comments.replace("\"", "\\\"");
        TextToSpeech.elevenLabsVoice("Vo4adEN1y46b0ufuysRe", comments);

        System.out.println("\n\u001B[36mAssistenten: " + comments + "\u001B[0m\n");

        String currentAiAssistantState = LlmResponseProcessor.getResponsePartString(followUpTextResponse, "ACTION");

        if (currentAiAssistantState.equals("ADD")) {
            for (int i = 0; i < crmData.size(); i++) {
                String crmInputText = String.format("{\"organization\":\"%s\",\"contact\":\"%s\",\"date\":\"%s\",\"summary\":\"%s\"}", crmData.get(i)[0], crmData.get(i)[1], crmData.get(i)[2], crmData.get(i)[3]);
                SendToDatabase.postCrmEntry(crmInputText, "http://localhost:8080/api/crm-entries");
            }

            for (int i = 0; i < remindersData.size(); i++) {
                String reminderInputText = String.format("{\"date\":\"%s\",\"time\":\"%s\",\"title\":\"%s\",\"details\":\"%s\"}", remindersData.get(i)[0], remindersData.get(i)[1], remindersData.get(i)[2], remindersData.get(i)[3]);
                SendToDatabase.postCrmEntry(reminderInputText, "http://localhost:8081/api/reminder-entries");
            }
        }


    }
}