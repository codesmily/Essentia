package br.com.fiap.essentia.domain.model;

import lombok.*;

import java.time.Instant;
import java.time.OffsetDateTime;
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class NoteLog {

    private String id;
    private String noteId;
    private String userId;
    private String action;
    private String description;
    private String ipAddress;
    private String userAgent;
    private String sessionId;
    private String metadata;
    private Instant createdAt = Instant.now();

}