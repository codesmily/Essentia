package br.com.fiap.essentia.application.dto;

import br.com.fiap.essentia.domain.enums.EmotionTypes;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class EmotionRequestDTO {

    @NotNull
    private EmotionTypes emotionType;
}