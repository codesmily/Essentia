package br.com.fiap.essentia.domain.ports.emotions.repository;

import br.com.fiap.essentia.adapters.out.mongo.doc.EmotionDocument;
import br.com.fiap.essentia.domain.enums.EmotionTypes;
import br.com.fiap.essentia.domain.model.Emotion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface EmotionRepository {

    Emotion save(EmotionDocument emotion);

    Optional<Emotion> findById(String id);
    List<Emotion> findAll();
    Emotion save(Emotion emotion);
    void deleteById(String id);
    long count();
    List<Emotion> findByDeletedFalse();
    Optional<Emotion> findByIdAndDeletedFalse(String id);
    Page<Emotion> findByUserIdAndDeletedFalse(String userId, Pageable pageable);
    Page<Emotion> findByUserIdAndEmotionTypeAndDeletedFalse(String userId, EmotionTypes type, Pageable pageable);
    Page<Emotion> findByDeletedFalse(Pageable pageable);
    Page<Emotion> findByUserIdAndDeletedFalseAndDayFeltBetween(String userId, LocalDateTime start, LocalDateTime end, Pageable pageable);
    List<Emotion> findTopNByUserIdAndDeletedFalseOrderByDayFeltDesc(String userId, int n); // (não é suportado nativamente)
    Page<Emotion> findByUserIdAndDeletedFalseOrderByDayFeltDesc(String userId, Pageable pageable);
    boolean existsById(String id);
    List<Emotion> saveAll(List<Emotion> list);
}