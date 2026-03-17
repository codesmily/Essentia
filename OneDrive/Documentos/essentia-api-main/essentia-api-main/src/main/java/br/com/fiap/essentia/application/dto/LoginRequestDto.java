package br.com.fiap.essentia.application.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequestDto {

    @jakarta.validation.constraints.Email
    @jakarta.validation.constraints.NotBlank
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private String email;

    @jakarta.validation.constraints.NotBlank
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
    private String password;
}

