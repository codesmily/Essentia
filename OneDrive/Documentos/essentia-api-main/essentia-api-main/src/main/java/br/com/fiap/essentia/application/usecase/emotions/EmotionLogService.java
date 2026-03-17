package br.com.fiap.essentia.application.usecase.emotions;

import br.com.fiap.essentia.domain.model.EmotionLog;
import br.com.fiap.essentia.domain.ports.emotions.repository.EmotionLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
@RequiredArgsConstructor
public class EmotionLogService {

    private final EmotionLogRepository emotionLogRepository;
    private final Logger logger = Logger.getLogger(EmotionLogService.class.getName());

    public EmotionLog salvarLog(EmotionLog log) {
        if (log.getOccurredAt() == null) {
            log.setOccurredAt(Instant.now());
        }
        return emotionLogRepository.save(log);
    }


    public void audit(String action, String userId, String description) {
        try {
            EmotionLog log = EmotionLog.builder()
                    .userId(userId)
                    .action(action)
                    .notes(description)              // guarda a descrição
                    .occurredAt(Instant.now())
                    .build();
            salvarLog(log);
        } catch (Exception e) {
            logger.log(Level.WARNING, "Falha ao salvar EmotionLog (básico): " + action, e);
        }
    }


    public void audit(
            String action,
            String userId,
            String emotionId,
            String oldEmotionType,
            String newEmotionType,
            String actorId,
            boolean success,
            String description
    ) {
        try {
            EmotionLog log = EmotionLog.builder()
                    .userId(userId)
                    .emotionId(emotionId)
                    .action(action)
                    .oldEmotionType(oldEmotionType)
                    .newEmotionType(newEmotionType)
                    .actorId(actorId)
                    .notes(buildNotes(success, description)) // agrega success + descrição
                    .occurredAt(Instant.now())
                    .build();
            salvarLog(log);
        } catch (Exception e) {
            logger.log(Level.WARNING, "Falha ao salvar EmotionLog (completo): " + action, e);
        }
    }

    private static String buildNotes(boolean success, String description) {
        String prefix = success ? "success: " : "error: ";
        return prefix + (description == null ? "" : description);
    }
}
