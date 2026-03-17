package br.com.fiap.essentia.application.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponseDto {
    private String token;
    private String tokenType = "Bearer";
    private long expiresInMs;
    private String userId;
    private String userName;
}