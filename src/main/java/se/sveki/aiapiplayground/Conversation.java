package se.sveki.aiapiplayground;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import se.sveki.aiapiplayground.utilities.PromptLoader;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

public class Conversation {

private String conversationStatus = "START";

    public void startConversation() throws IOException {

        YAMLMapper mapper = new YAMLMapper();
        JsonNode config = mapper.readTree(Main.class.getResourceAsStream("/config.yaml"));

//        TextToSpeech.elevenLabsVoice("Hej! Administratören här! Berätta vad du vill, så ser jag till att all information hamnar rätt. Min lott i livet är ju tyvärr att lyssna på ditt pladder och försöka få någon form av struktur i det hela...");

        String initialReport = "Jag hade just ett möte med Svante Jonsson på Testingbolaget och de är intresserade av att ta in tre konsulter efter sommaren. Vi har ett nytt möte 3:e juni 15:00 på deras kontor. Påminn mig om att jag behöver kolla med Hanna på torsdag förmiddag om hon är tillgänglig och att jag också måste kolla med Torbjörn i mitten av nästa vecka om han är intresserad av ett underkonsultuppdrag.";
//        try {
//            initialReport = SpeechToText.speechToText();
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }

        System.out.println(config.get("Presentation").get("UserTextColor").asText() + "Du: " + initialReport + config.get("Presentation").get("ResetTextColor").asText());

        String today = LocalDate.now().toString();

        String initialTextResponse = TextToText.textToText(
                PromptLoader.getPrompt("TextToText.OpenAI.Start").replace("{{today}}", LocalDate.now().toString()),
                initialReport);

//        System.out.println(initialTextResponse);

        String comments = LlmResponseProcessor.getResponsePartString(initialTextResponse, "COMMENTS");
        List<String[]> crmData = LlmResponseProcessor.getResponsePartList(initialTextResponse, "CRM");
        List<String[]> remindersData = LlmResponseProcessor.getResponsePartList(initialTextResponse, "REMINDER");

        System.out.println(config.get("Presentation").get("AssistantTextColor").asText()
                + "Assistenten: " + comments
                + config.get("Presentation").get("ResetTextColor").asText());

        System.out.println("\nFörslag för registrering i CRM:");
        LlmResponseProcessor.printListData(
                crmData,
                "CRM DATA",
                "Organisation",
                "Kontaktperson",
                "Datum",
                "Kommentar");

        System.out.println("\nFörslag för påminnelser att registrera:");
        LlmResponseProcessor.printListData(
                remindersData,
                "PÅMINNELSE",
                "Datum",
                "Tid",
                "Rubrik",
                "Detaljer");



        String safeComments = comments.replace("\"", "\\\"");
//        TextToSpeech.elevenLabsVoice(comments);

        String entryDraftReaction = "Det blir jättebra! Jag godkänner det!";

//        try {
//            entryDraftReaction = SpeechToText.speechToText();
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }

        System.out.println(config.get("Presentation").get("UserTextColor").asText()
                + "Du: " + entryDraftReaction
                + config.get("Presentation").get("ResetTextColor").asText());

        String followUpTextResponse = TextToText.textToText(
                PromptLoader.getPrompt("TextToText.OpenAI.FollowUp").replace("{{oldCrm}}", LlmResponseProcessor.getResponsePartString(initialTextResponse, "CRM"))
                        .replace("{{oldReminder}}", LlmResponseProcessor.getResponsePartString(initialTextResponse, "REMINDER")),
                entryDraftReaction
        );

        comments = LlmResponseProcessor.getResponsePartString(followUpTextResponse, "COMMENTS");

//        TextToSpeech.elevenLabsVoice(comments);

        System.out.println(config.get("Presentation").get("AssistantTextColor").asText() + "Assistenten: " + comments + config.get("Presentation").get("ResetTextColor").asText());

        conversationStatus = LlmResponseProcessor.getResponsePartString(followUpTextResponse, "ACTION");

//        if (conversationStatus.equals("ADD")) {
//            for (int i = 0; i < crmData.size(); i++) {
//                String crmInputText = String.format("{\"organization\":\"%s\",\"contact\":\"%s\",\"date\":\"%s\",\"summary\":\"%s\"}", crmData.get(i)[0], crmData.get(i)[1], crmData.get(i)[2], crmData.get(i)[3]);
//                SendToDatabase.postCrmEntry(crmInputText, "http://localhost:8080/api/crm-entries");
//            }
//
//            for (int i = 0; i < remindersData.size(); i++) {
//                String reminderInputText = String.format("{\"date\":\"%s\",\"time\":\"%s\",\"title\":\"%s\",\"details\":\"%s\"}", remindersData.get(i)[0], remindersData.get(i)[1], remindersData.get(i)[2], remindersData.get(i)[3]);
//                SendToDatabase.postCrmEntry(reminderInputText, "http://localhost:8081/api/reminder-entries");
//            }
//        }

    }

    public String getConversationStatus() {
        return conversationStatus;
    }

    public void setConversationStatus(String conversationStatus) {
        this.conversationStatus = conversationStatus;
    }
}
