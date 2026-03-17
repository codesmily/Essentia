package br.com.fiap.essentia.adapters.out.mongo.doc;

import br.com.fiap.essentia.domain.model.NoteLog;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.Instant;
import java.time.OffsetDateTime;


@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder(toBuilder = true)
@Document(collection = "note_logs")
public class NoteLogDocument implements MongoDocument<NoteLog> {

    @Id
    private String id;

    @NotBlank
    @Size(max = 50)
    @Field("note_id")
    private String noteId;

    @NotBlank
    @Size(max = 50)
    @Field("user_id")
    private String userId;

    @NotBlank
    @Size(max = 50)
    private String action;

    @Size(max = 2000)
    private String description;

    @Size(max = 45)
    @Pattern(
            regexp = "^(?:(?:25[0-5]|2[0-4]\\d|1\\d\\d|[1-9]?\\d)(?:\\.(?!$)|$)){4}$|^(?:[A-Fa-f0-9]{1,4}:){7}[A-Fa-f0-9]{1,4}$",
            message = "Endereço IP inválido"
    )
    @Field("ip_address")
    private String ipAddress;

    @Size(max = 512)
    @Field("user_agent")
    private String userAgent;

    @Size(max = 128)
    @Field("session_id")
    private String sessionId;

    @Size(max = 4096)
    private String metadata;

    @NotNull
    @PastOrPresent
    @CreatedDate
    @Field("created_at")
    private Instant createdAt;

    public static NoteLogDocument fromDomain(NoteLog noteLog) {
        if (noteLog == null) return null;
        return NoteLogDocument.builder()
                .id(noteLog.getId())
                .noteId(noteLog.getNoteId())
                .userId(noteLog.getUserId())
                .action(noteLog.getAction())
                .description(noteLog.getDescription())
                .ipAddress(noteLog.getIpAddress())
                .userAgent(noteLog.getUserAgent())
                .sessionId(noteLog.getSessionId())
                .metadata(noteLog.getMetadata())
                .createdAt(noteLog.getCreatedAt())
                .build();
    }

    @Override
    public NoteLog toDomain() {
        return NoteLog.builder()
                .id(id)
                .noteId(noteId)
                .userId(userId)
                .action(action)
                .description(description)
                .ipAddress(ipAddress)
                .userAgent(userAgent)
                .sessionId(sessionId)
                .metadata(metadata)
                .createdAt(createdAt)
                .build();
    }


}