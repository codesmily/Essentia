package br.com.fiap.essentia.domain.ports.notes.repository;

import br.com.fiap.essentia.domain.model.NoteLog;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface NoteLogRepository {

    Optional<NoteLog> findById(String id);

    List<NoteLog> findByUserId(String userId);

    List<NoteLog> findByUserIdAndDateRange(String userId, OffsetDateTime startDate, OffsetDateTime endDate);

    List<NoteLog> findByUserIdOrderByCreatedAtDesc(String userId);

    NoteLog save(NoteLog noteLog);

    void deleteById(String id);

    void deleteByUserId(String userId);

    List<NoteLog> findAll();

    long countByUserId(String userId);
}