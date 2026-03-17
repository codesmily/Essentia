package br.com.fiap.essentia.adapters.out.mongo.impl;

import br.com.fiap.essentia.adapters.out.mongo.doc.NoteDocument;
import br.com.fiap.essentia.adapters.out.mongo.repo.SpringDataNoteRepository;
import br.com.fiap.essentia.domain.enums.EmotionTypes;
import br.com.fiap.essentia.domain.model.Note;
import br.com.fiap.essentia.domain.ports.notes.repository.NoteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class MongoNoteRepository implements NoteRepository {

    private final SpringDataNoteRepository repository;


    @Override
    public Optional<Note> findById(String id) {
        return repository.findById(id).map(NoteDocument::toDomain);
    }

    @Override
    public Note save(Note note) {
        NoteDocument saved = repository.save(NoteDocument.fromDomain(note));
        return saved.toDomain();
    }

    @Override
    public void deleteById(String id) {
        repository.deleteById(id);
    }

    @Override
    public boolean existsById(String id) {
        return repository.existsById(id);
    }

    @Override
    public List<Note> findAll() {
        return repository.findAll().stream().map(NoteDocument::toDomain).toList();
    }


    @Override
    public List<Note> findByDeletedFalse() {
        return repository.findByDeletedFalse().stream().map(NoteDocument::toDomain).toList();
    }

    @Override
    public Page<Note> findByDeletedFalse(Pageable pageable) {
        return repository.findByDeletedFalse(pageable).map(NoteDocument::toDomain);
    }

    @Override
    public Page<Note> findByUserIdAndDeletedFalse(String userId, Pageable pageable) {
        return repository.findByUserIdAndDeletedFalse(userId, pageable).map(NoteDocument::toDomain);
    }

    @Override
    public Page<Note> findByUserIdAndCurrentEmotionAndDeletedFalse(String userId, EmotionTypes currentEmotion, Pageable pageable) {
        return repository.findByUserIdAndCurrentEmotionAndDeletedFalse(userId, currentEmotion, pageable).map(NoteDocument::toDomain);
    }

    @Override
    public Page<Note> findByUserIdAndDeletedFalseAndCreatedAtBetween(String userId, Instant startInclusive, Instant endExclusive, Pageable pageable) {
        return repository.findByUserIdAndDeletedFalseAndCreatedAtBetween(userId, startInclusive, endExclusive, pageable).map(NoteDocument::toDomain);
    }


    @Override
    public List<Note> findByUserId(String userId) {
        return repository.findByUserId(userId).stream().map(NoteDocument::toDomain).toList();
    }

    @Override
    public List<Note> findByUserIdAndDateRange(String userId, OffsetDateTime startDate, OffsetDateTime endDate) {
        // Se ainda precisar desse legado, converta para Instant para reaproveitar o índice de createdAt (Instant)
        Instant start = startDate.toInstant();
        Instant end = endDate.toInstant();
        // Use uma consulta paginada “unpaged” para reaproveitar o método novo
        return repository.findByUserIdAndDeletedFalseAndCreatedAtBetween(userId, start, end, Pageable.unpaged()).map(NoteDocument::toDomain).getContent();
    }

    @Override
    public List<Note> findByUserIdOrderByCreatedAtDesc(String userId) {
        return repository.findByUserIdOrderByCreatedAtDesc(userId).stream().map(NoteDocument::toDomain).toList();
    }

    @Override
    public void deleteByUserId(String userId) {
        repository.deleteByUserId(userId);
    }

    @Override
    public long countByUserId(String userId) {
        return repository.countByUserId(userId);
    }
}
