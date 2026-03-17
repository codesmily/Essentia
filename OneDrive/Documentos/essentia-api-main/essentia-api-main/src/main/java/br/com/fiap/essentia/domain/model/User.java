package br.com.fiap.essentia.domain.model;

import lombok.*;

import java.time.OffsetDateTime;
import java.util.List;


@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class User {

    private String id;
    private String name;
    private String email;
    private String passwordHash;
    private Boolean active;
    private OffsetDateTime lastLogin;
    private List<String> roles;


}
