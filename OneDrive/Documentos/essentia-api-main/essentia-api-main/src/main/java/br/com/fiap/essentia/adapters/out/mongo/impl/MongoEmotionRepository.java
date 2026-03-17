package br.com.fiap.essentia.adapters.out.mongo.impl;

import br.com.fiap.essentia.adapters.out.mongo.doc.EmotionDocument;
import br.com.fiap.essentia.adapters.out.mongo.repo.SpringDataEmotionRepository;
import br.com.fiap.essentia.domain.enums.EmotionTypes;
import br.com.fiap.essentia.domain.model.Emotion;
import br.com.fiap.essentia.domain.ports.emotions.repository.EmotionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class MongoEmotionRepository implements EmotionRepository {

    private final SpringDataEmotionRepository repository;


    @Override
    public Emotion save(Emotion emotion) {
        EmotionDocument saved = repository.save(EmotionDocument.fromDomain(emotion));
        return saved.toDomain();
    }

    @Override
    public Emotion save(EmotionDocument emotion) {
        return null;
    }

    @Override
    public Optional<Emotion> findById(String id) {
        return repository.findById(id).map(EmotionDocument::toDomain);
    }

    @Override
    public List<Emotion> findAll() {
        return repository.findAll().stream().map(EmotionDocument::toDomain).toList();
    }

    @Override
    public void deleteById(String id) {
        repository.deleteById(id);
    }

    @Override
    public long count() {
        return repository.count();
    }

    @Override
    public List<Emotion> findByDeletedFalse() {
        return repository.findByDeletedFalse().stream().map(EmotionDocument::toDomain).toList();
    }


    @Override
    public Optional<Emotion> findByIdAndDeletedFalse(String id) {
        return repository.findByIdAndDeletedFalse(id).map(EmotionDocument::toDomain);
    }

    @Override
    public Page<Emotion> findByUserIdAndDeletedFalse(String userId, Pageable pageable) {
        return repository.findByUserIdAndDeletedFalse(userId, pageable).map(EmotionDocument::toDomain);
    }

    @Override
    public Page<Emotion> findByUserIdAndEmotionTypeAndDeletedFalse(String userId, EmotionTypes type, Pageable pageable) {
        return repository.findByUserIdAndEmotionTypeAndDeletedFalse(userId, type, pageable).map(EmotionDocument::toDomain);
    }

    @Override
    public Page<Emotion> findByDeletedFalse(Pageable pageable) {
        return repository.findByDeletedFalse(pageable).map(EmotionDocument::toDomain);
    }

    @Override
    public Page<Emotion> findByUserIdAndDeletedFalseAndDayFeltBetween(String userId, LocalDateTime start, LocalDateTime end, Pageable pageable) {
        return repository.findByUserIdAndDeletedFalseAndDayFeltBetween(userId, start, end, pageable).map(EmotionDocument::toDomain);
    }

    @Override
    public List<Emotion> findTopNByUserIdAndDeletedFalseOrderByDayFeltDesc(String userId, int n) {
        Pageable topN = PageRequest.of(0, n, Sort.by(Sort.Direction.DESC, "dayFelt"));
        // Se o SpringData repo devolver Page<EmotionDocument>
        return repository.findByUserIdAndDeletedFalseOrderByDayFeltDesc(userId, topN).map(EmotionDocument::toDomain).getContent();
    }

    @Override
    public Page<Emotion> findByUserIdAndDeletedFalseOrderByDayFeltDesc(String userId, Pageable pageable) {
        return repository.findByUserIdAndDeletedFalseOrderByDayFeltDesc(userId, pageable).map(EmotionDocument::toDomain);
    }

    @Override
    public boolean existsById(String id) {
        return repository.existsById(id);
    }

    @Override
    public List<Emotion> saveAll(List<Emotion> list) {
        return repository.saveAll(list.stream().map(EmotionDocument::fromDomain).toList()).stream().map(EmotionDocument::toDomain).toList();
    }


    public Optional<Emotion> softDelete(String id) {
        return repository.findById(id).map(doc -> {
            doc.setDeleted(true);
            return repository.save(doc).toDomain();
        });
    }

    public List<Emotion> findAllActive() {
        return repository.findByDeletedFalse()               // List<EmotionDocument>
                .stream().map(EmotionDocument::toDomain).toList();
    }
}
