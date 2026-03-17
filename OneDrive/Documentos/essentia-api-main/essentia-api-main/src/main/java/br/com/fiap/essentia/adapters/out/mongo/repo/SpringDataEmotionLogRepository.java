// adapters/out/mongo/repo/SpringDataEmotionLogRepository.java
package br.com.fiap.essentia.adapters.out.mongo.repo;

import br.com.fiap.essentia.adapters.out.mongo.doc.EmotionLogDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.Instant;
import java.util.List;

public interface SpringDataEmotionLogRepository extends MongoRepository<EmotionLogDocument, String> {

    List<EmotionLogDocument> findByUserId(String userId);

    List<EmotionLogDocument> findByUserIdAndOccurredAtBetween(String userId, Instant startInclusive, Instant endInclusive);

    List<EmotionLogDocument> findByUserIdOrderByOccurredAtDesc(String userId);

    void deleteByUserId(String userId);

    long countByUserId(String userId);
}
