package br.com.fiap.essentia.domain.ports.notes.repository;

import br.com.fiap.essentia.domain.enums.EmotionTypes;
import br.com.fiap.essentia.domain.model.Note;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface NoteRepository {

    /* ===== CRUD básico ===== */
    Optional<Note> findById(String id);

    Note save(Note note);

    void deleteById(String id);

    boolean existsById(String id);

    List<Note> findAll();



    List<Note> findByDeletedFalse();


    Page<Note> findByDeletedFalse(Pageable pageable);

    Page<Note> findByUserIdAndDeletedFalse(String userId, Pageable pageable);

    Page<Note> findByUserIdAndCurrentEmotionAndDeletedFalse(String userId, EmotionTypes currentEmotion, Pageable pageable);


    Page<Note> findByUserIdAndDeletedFalseAndCreatedAtBetween(String userId, Instant startInclusive, Instant endExclusive, Pageable pageable);


    List<Note> findByUserId(String userId);

    List<Note> findByUserIdAndDateRange(String userId,
                                        OffsetDateTime startDate,
                                        OffsetDateTime endDate);

    List<Note> findByUserIdOrderByCreatedAtDesc(String userId);

    void deleteByUserId(String userId);

    long countByUserId(String userId);
}
