package br.com.fiap.essentia.adapters.out.mongo.impl;

import br.com.fiap.essentia.adapters.out.mongo.doc.NoteLogDocument;
import br.com.fiap.essentia.adapters.out.mongo.repo.SpringDataNoteLogRepository;
import br.com.fiap.essentia.domain.model.NoteLog;
import br.com.fiap.essentia.domain.ports.notes.repository.NoteLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class MongoNoteLogRepository implements NoteLogRepository {

    private final SpringDataNoteLogRepository repository;

    @Override
    public Optional<NoteLog> findById(String id) {
        return repository.findById(id).map(NoteLogDocument::toDomain);
    }

    @Override
    public List<NoteLog> findByUserId(String userId) {
        return repository.findByUserId(userId)
                .stream()
                .map(NoteLogDocument::toDomain)
                .toList();
    }

    @Override
    public List<NoteLog> findByUserIdAndDateRange(String userId, OffsetDateTime startDate, OffsetDateTime endDate) {
        return repository.findByUserIdAndCreatedAtBetween(
                        userId,
                        startDate.toInstant(),
                        endDate.toInstant()
                )
                .stream()
                .map(NoteLogDocument::toDomain)
                .toList();
    }

    @Override
    public List<NoteLog> findByUserIdOrderByCreatedAtDesc(String userId) {
        return repository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(NoteLogDocument::toDomain)
                .toList();
    }

    @Override
    public NoteLog save(NoteLog noteLog) {
        NoteLogDocument saved = repository.save(NoteLogDocument.fromDomain(noteLog));
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
    public List<NoteLog> findAll() {
        return repository.findAll()
                .stream()
                .map(NoteLogDocument::toDomain)
                .toList();
    }

    @Override
    public long countByUserId(String userId) {
        return repository.countByUserId(userId);
    }
}
