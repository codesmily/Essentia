package br.com.fiap.essentia.application.usecase.authUser;

import br.com.fiap.essentia.domain.model.UserLog;
import br.com.fiap.essentia.domain.ports.auth.repository.UserLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
public class UserLogService {

    private final UserLogRepository userLogRepository;

    public UserLog salvarLog(UserLog log) {
        if (log.getCreatedAt() == null) {
            log.setCreatedAt(Instant.now());
        }
        return userLogRepository.save(log);
    }

    public void audit(String action, String userId, String email, boolean success, String description) {
        try {
            UserLog log = UserLog.builder()
                    .userId(userId)
                    .action(action)
                    .description((success ? "success: " : "error: ") + description + (email != null ? " (" + email + ")" : ""))
                    .createdAt(Instant.now())
                    .build();
            salvarLog(log);
        } catch (Exception e) {

        }
    }


}