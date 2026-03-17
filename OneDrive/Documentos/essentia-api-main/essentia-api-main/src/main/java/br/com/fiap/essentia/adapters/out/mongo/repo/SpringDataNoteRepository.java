package br.com.fiap.essentia.adapters.out.mongo.repo;

import br.com.fiap.essentia.adapters.out.mongo.doc.NoteDocument;
import br.com.fiap.essentia.domain.enums.EmotionTypes;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.Instant;
import java.util.List;

public interface SpringDataNoteRepository extends MongoRepository<NoteDocument, String> {


    List<NoteDocument> findByDeletedFalse();

    Page<NoteDocument> findByDeletedFalse(Pageable pageable);

    Page<NoteDocument> findByUserIdAndDeletedFalse(String userId, Pageable pageable);

    Page<NoteDocument> findByUserIdAndCurrentEmotionAndDeletedFalse(String userId, EmotionTypes currentEmotion, Pageable pageable);

    Page<NoteDocument> findByUserIdAndDeletedFalseAndCreatedAtBetween(String userId, Instant startInclusive, Instant endExclusive, Pageable pageable);


    List<NoteDocument> findByUserId(String userId);


    List<NoteDocument> findByUserIdOrderByCreatedAtDesc(String userId);

    void deleteByUserId(String userId);

    long countByUserId(String userId);

    boolean existsById(String id);
}
