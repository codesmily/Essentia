package br.com.fiap.essentia.adapters.out.mongo.impl;

import br.com.fiap.essentia.adapters.out.mongo.doc.EmotionLogDocument;
import br.com.fiap.essentia.adapters.out.mongo.repo.SpringDataEmotionLogRepository;
import br.com.fiap.essentia.domain.model.EmotionLog;
import br.com.fiap.essentia.domain.ports.emotions.repository.EmotionLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class MongoEmotionLogRepository implements EmotionLogRepository {

    private final SpringDataEmotionLogRepository repository;

    @Override
    public Optional<EmotionLog> findById(String id) {
        return repository.findById(id).map(EmotionLogDocument::toDomain);
    }

    @Override
    public List<EmotionLog> findByUserId(String userId) {
        return repository.findByUserId(userId).stream().map(EmotionLogDocument::toDomain).toList();
    }

    @Override
    public List<EmotionLog> findByUserIdAndDateRange(String userId, OffsetDateTime startDate, OffsetDateTime endDate) {
        return repository.findByUserIdAndOccurredAtBetween(userId, startDate.toInstant(), endDate.toInstant()).stream().map(EmotionLogDocument::toDomain).toList();
    }

    @Override
    public List<EmotionLog> findByUserIdOrderByCreatedAtDesc(String userId) {
        return repository.findByUserIdOrderByOccurredAtDesc(userId).stream().map(EmotionLogDocument::toDomain).toList();
    }

    @Override
    public EmotionLog save(EmotionLog emotionLog) {
        EmotionLogDocument saved = repository.save(EmotionLogDocument.fromDomain(emotionLog));
        return saved.toDomain();
    }

    @Override
    public void deleteById(String id) {
        repository.deleteById(id);
    }

    @Override
    public void deleteByUserId(String userId) {
        repository.deleteByUserId(userId);
    }

    @Override
    public List<EmotionLog> findAll() {
        return repository.findAll().stream().map(EmotionLogDocument::toDomain).toList();
    }

    @Override
    public long countByUserId(String userId) {
        return repository.countByUserId(userId);
    }
}
