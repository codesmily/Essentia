package br.com.fiap.essentia.domain.model;

import br.com.fiap.essentia.domain.enums.EmotionTypes;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Document(collection = "emotions")
public class Emotion {

    @Id
    private String id;
    private EmotionTypes emotionType;
    private LocalDateTime dayFelt;
    private String userId;
    private boolean deleted = false;

}
