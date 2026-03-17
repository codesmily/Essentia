package br.com.fiap.essentia.adapters.out.mongo.doc;

import br.com.fiap.essentia.domain.enums.EmotionTypes;
import br.com.fiap.essentia.domain.model.Note;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.Instant;

@Document("notes")
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Builder(toBuilder = true)
public class NoteDocument implements MongoDocument<Note> {
    @Id
    private String id;

    private String title;
    private String content;

    @CreatedDate
    private Instant createdAt;

    @NotBlank
    @Size(max = 50)
    @Field("user_id")
    @Schema(type = "string", nullable = false)
    private String userId;

    private EmotionTypes currentEmotion;

    private boolean deleted;

    public static NoteDocument fromDomain(Note note) {
        if (note == null) return null;
        return NoteDocument.builder()
                .id(note.getId())
                .title(note.getTitle())
                .content(note.getContent())
                .createdAt(note.getCreatedAt())        // mantém createdAt se já vier setado
                .userId(note.getUserId())              // <<< FALTAVA
                .currentEmotion(note.getCurrentEmotion())
                .deleted(note.isDeleted())
                .build();
    }

    @Override
    public Note toDomain() {
        return Note.builder()
                .id(id)
                .title(title)
                .content(content)
                .createdAt(createdAt)
                .userId(userId)                        // <<< FALTAVA
                .currentEmotion(currentEmotion)
                .deleted(deleted)
                .build();
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }
}