package br.com.fiap.essentia.adapters.out.mongo.repo;

import br.com.fiap.essentia.adapters.out.mongo.doc.EmotionDocument;
import br.com.fiap.essentia.domain.enums.EmotionTypes;
import br.com.fiap.essentia.domain.model.Emotion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface SpringDataEmotionRepository extends MongoRepository<EmotionDocument, String> {

    // Básicos
    List<EmotionDocument> findByDeletedFalse();

    Optional<EmotionDocument> findByIdAndDeletedFalse(String id);

    // Paginados
    Page<EmotionDocument> findByDeletedFalse(Pageable pageable);

    Page<EmotionDocument> findByUserIdAndDeletedFalse(String userId, Pageable pageable);

    Page<EmotionDocument> findByUserIdAndEmotionTypeAndDeletedFalse(String userId, EmotionTypes type, Pageable pageable);

    Page<EmotionDocument> findByUserIdAndDeletedFalseAndDayFeltBetween(String userId, LocalDateTime start, LocalDateTime end, Pageable pageable);

    // Ordenação por data (timeline)
    Page<EmotionDocument> findByUserIdAndDeletedFalseOrderByDayFeltDesc(String userId, Pageable pageable);
}
