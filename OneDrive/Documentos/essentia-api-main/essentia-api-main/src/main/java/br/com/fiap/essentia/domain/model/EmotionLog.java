package br.com.fiap.essentia.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "emotion_logs")
public class EmotionLog {

    @Id
    private String id;

    @Indexed
    private String emotionId;

    @Indexed
    private String userId;

    private String action;

    private String oldEmotionType;
    private String newEmotionType;

    @Builder.Default
    private Instant occurredAt = Instant.now();

    private String actorId;

    private String notes;

}