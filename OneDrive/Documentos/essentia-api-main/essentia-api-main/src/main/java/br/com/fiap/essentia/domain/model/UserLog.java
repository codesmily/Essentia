package br.com.fiap.essentia.domain.model;

import lombok.*;

import java.time.Instant;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class UserLog {

    private String id;
    private String userId;
    private String action;
    private String description;
    private String ipAddress;
    private String userAgent;
    private String sessionId;
    private String metadata;
    private Instant createdAt = Instant.now();

}