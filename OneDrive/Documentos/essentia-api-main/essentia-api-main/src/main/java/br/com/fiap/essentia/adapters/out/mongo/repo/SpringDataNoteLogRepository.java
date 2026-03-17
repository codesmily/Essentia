package br.com.fiap.essentia.adapters.out.mongo.repo;

import br.com.fiap.essentia.adapters.out.mongo.doc.NoteLogDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.Instant;
import java.util.List;

public interface SpringDataNoteLogRepository extends MongoRepository<NoteLogDocument, String> {

    List<NoteLogDocument> findByUserId(String userId);

    List<NoteLogDocument> findByUserIdAndCreatedAtBetween(String userId, Instant startInclusive, Instant endExclusive);

    List<NoteLogDocument> findByUserIdOrderByCreatedAtDesc(String userId);

    void deleteByUserId(String userId);

    long countByUserId(String userId);
}
