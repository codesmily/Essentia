package br.com.fiap.essentia.adapters.out.mongo.doc;

import br.com.fiap.essentia.domain.enums.EmotionTypes;
import br.com.fiap.essentia.domain.model.Emotion;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
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

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "emotions")
public class EmotionDocument implements MongoDocument<Emotion> {

    @Id
    private String id;

    @NotNull
    private EmotionTypes emotionType;

    @NotNull
    @PastOrPresent
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
    private LocalDateTime dayFelt;

    @NotBlank
    @Size(max = 50)
    private String userId;

    private boolean deleted;

    public static EmotionDocument fromDomain(Emotion emotion) {
        if (emotion == null) return null;
        return EmotionDocument.builder()
                .id(emotion.getId())
                .emotionType(emotion.getEmotionType())
                .dayFelt(emotion.getDayFelt())
                .userId(emotion.getUserId())
                .deleted(emotion.isDeleted())
                .build();
    }

    @Override
    public Emotion toDomain() {
        return Emotion.builder()
                .id(id)
                .emotionType(emotionType)
                .dayFelt(dayFelt)
                .userId(userId)
                .deleted(deleted)
                .build();
    }
}

