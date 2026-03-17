package br.com.fiap.essentia.domain.model;

import br.com.fiap.essentia.domain.enums.EmotionTypes;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class Note {

    private String id;
    private String title;
    private String content;
    private Instant createdAt;
    private String userId;
    private EmotionTypes currentEmotion;
    private boolean deleted;
}