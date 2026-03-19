package se.sveki.aiapiplayground.utilities;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.InputStream;

public class PromptLoader {
    private static final ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());

    public static String getPrompt(String key) {
        try (InputStream inputStream = PromptLoader.class.getResourceAsStream("/prompts.yaml")) {
            if (inputStream == null) {
                throw new RuntimeException("Hittade inte prompts.yaml i resources!");
            }

            JsonNode node = yamlMapper.readTree(inputStream);

            // Dela upp "TextToText.OpenAI.Start" till ["TextToText", "OpenAI", "Start"]
            String[] parts = key.split("\\.");

            for (String part : parts) {
                node = node.path(part);
            }

            if (node.isMissingNode()) {
                throw new RuntimeException("Kunde inte hitta prompt-vägen: " + key);
            }

            return node.asText();
        } catch (Exception e) {
            throw new RuntimeException("Fel vid laddning av prompt: " + e.getMessage(), e);
        }
    }
}
