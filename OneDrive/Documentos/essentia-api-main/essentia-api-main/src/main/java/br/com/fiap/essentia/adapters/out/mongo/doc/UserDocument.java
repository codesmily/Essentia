package br.com.fiap.essentia.adapters.out.mongo.doc;

import br.com.fiap.essentia.domain.model.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;


@Document("users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class UserDocument implements MongoDocument<User> {
    @Id
    private String id;
    private String name;
    @Indexed(unique = true)
    private String email;
    private String passwordHash;
    private Boolean active;
    @CreatedDate
    private Instant createdAt;
    @LastModifiedDate
    private Instant updatedAt;
    private Instant lastLogin;

    public static UserDocument fromDomain(User user) {
        if (user == null) return null;
        return UserDocument.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .passwordHash(user.getPasswordHash())
                .active(user.getActive())
                .lastLogin(user.getLastLogin() != null ? user.getLastLogin().toInstant() : null)
                .build();
    }
    @Override
    public User toDomain() {
        return User.builder()
                .id(id)
                .name(name)
                .email(email)
                .passwordHash(passwordHash)
                .active(active)
                .lastLogin(lastLogin != null ? OffsetDateTime.ofInstant(lastLogin, ZoneOffset.UTC) : null)
                .build();
    }
}

