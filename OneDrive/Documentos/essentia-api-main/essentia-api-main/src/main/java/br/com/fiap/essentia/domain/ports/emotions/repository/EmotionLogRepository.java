package br.com.fiap.essentia.domain.ports.emotions.repository;

import br.com.fiap.essentia.adapters.out.mongo.doc.EmotionLogDocument;
import br.com.fiap.essentia.domain.model.EmotionLog;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface EmotionLogRepository {

    Optional<EmotionLog> findById(String id);

    List<EmotionLog> findByUserId(String userId);

    List<EmotionLog> findByUserIdAndDateRange(String userId, OffsetDateTime startDate, OffsetDateTime endDate);

    List<EmotionLog> findByUserIdOrderByCreatedAtDesc(String userId);

    EmotionLog save(EmotionLog emotionLog);

    void deleteById(String id);

    void deleteByUserId(String userId);

    List<EmotionLog> findAll();

    long countByUserId(String userId);
}