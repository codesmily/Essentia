package br.com.fiap.essentia.adapters.out.mongo.doc;

import br.com.fiap.essentia.domain.model.EmotionLog;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.Instant;
import java.time.OffsetDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder(toBuilder = true)
@Document(collection = "emotion_logs")
public class EmotionLogDocument implements MongoDocument<EmotionLog> {

    @Id
    private String id;

    @NotBlank
    @Size(max = 50)
    @Field("emotion_id")
    private String emotionId;

    @NotBlank
    @Size(max = 50)
    @Field("user_id")
    private String userId;

    @NotNull
    @Field("action")
    private String action;

    @Size(max = 50)
    @Field("old_emotion_type")
    private String oldEmotionType;

    @Size(max = 50)
    @Field("new_emotion_type")
    private String newEmotionType;

    @NotNull
    @PastOrPresent
    @Field("occurred_at")
    private Instant occurredAt;

    @Size(max = 50)
    @Field("actor_id")
    private String actorId;

    @Size(max = 255)
    @Field("notes")
    private String notes;

    public static EmotionLogDocument fromDomain(EmotionLog emotionLog) {
        if (emotionLog == null) return null;
        return EmotionLogDocument.builder()
                .id(emotionLog.getId())
                .emotionId(emotionLog.getEmotionId())
                .userId(emotionLog.getUserId())
                .action(emotionLog.getAction())
                .oldEmotionType(emotionLog.getOldEmotionType())
                .newEmotionType(emotionLog.getNewEmotionType())
                .occurredAt(emotionLog.getOccurredAt())
                .actorId(emotionLog.getActorId())
                .notes(emotionLog.getNotes())
                .build();
    }

    @Override
    public EmotionLog toDomain() {
        return EmotionLog.builder()
                .id(id)
                .emotionId(emotionId)
                .userId(userId)
                .action(action)
                .oldEmotionType(oldEmotionType)
                .newEmotionType(newEmotionType)
                .occurredAt(occurredAt)
                .actorId(actorId)
                .notes(notes)
                .build();
    }
}