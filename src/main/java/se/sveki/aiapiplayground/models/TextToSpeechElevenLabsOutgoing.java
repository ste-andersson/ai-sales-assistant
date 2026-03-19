package se.sveki.aiapiplayground.models;

import com.fasterxml.jackson.annotation.JsonProperty;

public record TextToSpeechElevenLabsOutgoing(
        String text,
        @JsonProperty("model_id") String modelId,
        @JsonProperty("voice_settings") VoiceSettings voiceSettings
) {
    public record VoiceSettings(
            double stability,
            @JsonProperty("similarity_boost") double similarityBoost
    ) {}
}