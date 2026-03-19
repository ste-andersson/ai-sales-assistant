package se.sveki.aiapiplayground.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record SpeechToTextOpenAiIncoming(String text) {
}
