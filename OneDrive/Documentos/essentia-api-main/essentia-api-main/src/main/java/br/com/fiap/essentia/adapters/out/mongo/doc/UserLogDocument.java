package br.com.fiap.essentia.adapters.out.mongo.doc;

import br.com.fiap.essentia.domain.model.UserLog;
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
@Document(collection = "user_logs")
public class UserLogDocument implements MongoDocument<UserLog> {

    @Id
    private String id;

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

    public static UserLogDocument fromDomain(UserLog userLog) {
        if (userLog == null) return null;
        return UserLogDocument.builder()
                .id(userLog.getId())
                .userId(userLog.getUserId())
                .action(userLog.getAction())
                .description(userLog.getDescription())
                .ipAddress(userLog.getIpAddress())
                .userAgent(userLog.getUserAgent())
                .sessionId(userLog.getSessionId())
                .metadata(userLog.getMetadata())
                .createdAt(userLog.getCreatedAt())
                .build();
    }

    @Override
    public UserLog toDomain() {
        return UserLog.builder()
                .id(id)
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