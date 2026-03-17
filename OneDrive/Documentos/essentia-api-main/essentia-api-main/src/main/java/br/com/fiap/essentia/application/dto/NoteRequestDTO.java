package br.com.fiap.essentia.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NoteRequestDTO {
    @NotNull @NotBlank(message = "O título é obrigatório")
    private String title;
    @NotNull @NotBlank(message = "O conteúdo é obrigatório")
    private String content;
}