package se.sveki.aiapiplayground;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import se.sveki.aiapiplayground.utilities.PromptLoader;

import java.time.LocalDate;
import java.util.List;

public class Main {
    public static void main(String[] args) throws Exception {

        Conversation conversation = new Conversation();
        conversation.startConversation();

    }
}