package se.sveki.aiapiplayground;

import java.util.ArrayList;
import java.util.List;

public class LlmResponseProcessor {

    public static String getResponsePartString(String llmResponse, String keyword) {
        int startPosition = llmResponse.indexOf("<" + keyword + ">") + keyword.length() + 2;
        int endPosition = llmResponse.lastIndexOf("</" + keyword + ">");

        if (startPosition == -1 || endPosition == -1) {
            return "";
        }

        return llmResponse.substring(startPosition, endPosition).replace("\"", "\\\"").replace("\n", "\\n");
    }

    public static List<String[]> getResponsePartList(String llmResponse, String keyword) {
        List<String[]> reminders = new ArrayList<>();

        for (String part : llmResponse.split("<" + keyword + ">")) {
            if (part.contains("</" + keyword + ">")) {
                String content = part.split("</" + keyword + ">")[0];
                reminders.add(content.split(";"));
            }
        }
        return reminders;
    }



    public static void printListData(List<String[]> inputData, String type, String label1, String label2, String label3, String label4) {
        for (int i = 0; i < inputData.size(); i++) {
            System.out.println(
                    type + " " + (i + 1) + "\n"
                            + label1 + ": " + inputData.get(i)[0] + "\n"
                            + label2 + ": " + inputData.get(i)[1] + "\n"
                            + label3 + ": " + inputData.get(i)[2] + "\n"
                            + label4 + ": " + inputData.get(i)[3] + "\n");
        }
    }

}
