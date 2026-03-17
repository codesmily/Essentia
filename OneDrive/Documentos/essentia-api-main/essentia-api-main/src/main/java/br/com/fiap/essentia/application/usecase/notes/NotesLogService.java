package br.com.fiap.essentia.application.usecase.notes;

import br.com.fiap.essentia.domain.model.NoteLog;
import br.com.fiap.essentia.domain.ports.notes.repository.NoteLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.logging.Level;
import java.util.logging.Logger;

@Service
@RequiredArgsConstructor
public class NotesLogService {

    private static final int MAX_DESC_LEN = 2000; // evita logs gigantes no banco

    private final NoteLogRepository noteLogRepository;
    private final Logger logger = Logger.getLogger(NotesLogService.class.getName());

    public NoteLog salvarLog(NoteLog log) {
        if (log.getCreatedAt() == null) {
            log.setCreatedAt(Instant.now());
        }
        return noteLogRepository.save(log);
    }


    public void audit(String action, String noteId, String userId, boolean success, String description, String ipAddress, String userAgent, String sessionId, String metadata) {
        try {
            NoteLog log = NoteLog.builder().noteId(nullIfBlank(noteId)).userId(nullIfBlank(userId)).action(nullIfBlank(action)).description(formatDescription(success, description)).ipAddress(nullIfBlank(ipAddress)).userAgent(nullIfBlank(userAgent)).sessionId(nullIfBlank(sessionId)).metadata(nullIfBlank(metadata)).createdAt(Instant.now()).build();

            salvarLog(log);
        } catch (Exception e) {
            logger.log(Level.WARNING, "Falha ao salvar NoteLog (action=" + action + "): " + e.getMessage(), e);
        }
    }


    public void audit(String action, String noteId, String userId, boolean success, String description) {
        audit(action, noteId, userId, success, description, null, null, null, null);
    }


    private static String formatDescription(boolean success, String description) {
        String base = (success ? "success: " : "error: ") + (description == null ? "" : description.trim());
        return truncate(base, MAX_DESC_LEN);
    }

    private static String truncate(String s, int max) {
        if (s == null || s.length() <= max) return s;
        return s.substring(0, Math.max(0, max - 3)) + "...";
    }

    private static String nullIfBlank(String s) {
        return (s == null || s.isBlank()) ? null : s;
    }
}
