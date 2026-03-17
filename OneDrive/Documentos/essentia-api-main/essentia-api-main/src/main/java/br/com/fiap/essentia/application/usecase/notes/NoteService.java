package br.com.fiap.essentia.application.usecase.notes;

import br.com.fiap.essentia.application.dto.NoteRequestDTO;
import br.com.fiap.essentia.domain.enums.EmotionTypes;
import br.com.fiap.essentia.domain.model.Emotion;
import br.com.fiap.essentia.domain.model.Note;
import br.com.fiap.essentia.domain.ports.emotions.repository.EmotionRepository;
import br.com.fiap.essentia.domain.ports.notes.repository.NoteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class NoteService {

    private final NoteRepository noteRepository;
    private final EmotionRepository emotionRepository;

    private String currentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null) {
            throw new IllegalStateException("Usuário não autenticado.");
        }
        return auth.getName();
    }

    /* ===== CRUD ===== */

    public List<Note> getAll() {
        return noteRepository.findByDeletedFalse();
    }

    public Optional<Note> getById(String id) {
        return noteRepository.findById(id).filter(n -> !n.isDeleted());
    }

    public Note create(NoteRequestDTO dto) {
        String userId = currentUserId();
        log.info(userId);
        Note note = new Note();
        note.setTitle(dto.getTitle());
        note.setContent(dto.getContent());
        note.setUserId(userId);
        note.setDeleted(false);
        note.setCreatedAt(Instant.now());

        EmotionTypes lastEmotion = emotionRepository
                .findTopNByUserIdAndDeletedFalseOrderByDayFeltDesc(userId, 1)
                .stream()
                .findFirst()
                .map(Emotion::getEmotionType)
                .orElse(null);
        note.setCurrentEmotion(lastEmotion);
        log.info(note.toString());
        return noteRepository.save(note);
    }

    public Note update(String id, NoteRequestDTO dto) {
        // (opcional) validar ownership aqui, se necessário
        return noteRepository.findById(id)
                .map(note -> {
                    if (note.isDeleted()) {
                        throw new RuntimeException("Nota já foi deletada.");
                    }
                    // se quiser validar: Objects.equals(note.getUserId(), currentUserId())
                    note.setTitle(dto.getTitle());
                    note.setContent(dto.getContent());
                    return noteRepository.save(note);
                })
                .orElseThrow(() -> new RuntimeException("Nota não encontrado com id: " + id));
    }

    public void softDelete(String id) {
        noteRepository.findById(id).ifPresent(note -> {
            note.setDeleted(true);
            noteRepository.save(note);
        });
    }

    public Note restore(String id) {
        Note note = noteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Nota não encontrada com id: " + id));
        if (!note.isDeleted()) return note;
        note.setDeleted(false);
        return noteRepository.save(note);
    }

    public void hardDelete(String id) {
        if (!noteRepository.existsById(id)) {
            throw new RuntimeException("Nota não encontrada com id: " + id);
        }
        noteRepository.deleteById(id);
    }

    /* ===== Consultas “minhas” (JWT) ===== */

    public Page<Note> getMinePaged(int page, int size, Sort sort) {
        Pageable pageable = PageRequest.of(page, size, sort);
        return noteRepository.findByUserIdAndDeletedFalse(currentUserId(), pageable);
    }

    public Page<Note> searchMyByEmotion(EmotionTypes emotion, int page, int size, Sort sort) {
        Pageable pageable = PageRequest.of(page, size, sort);
        return noteRepository.findByUserIdAndCurrentEmotionAndDeletedFalse(currentUserId(), emotion, pageable);
    }

    public Page<Note> getMyByDateRange(LocalDate start, LocalDate end, int page, int size, Sort sort) {
        if (end.isBefore(start)) {
            throw new IllegalArgumentException("Data final não pode ser anterior à inicial");
        }
        Instant startInstant = start.atStartOfDay(ZoneOffset.UTC).toInstant();
        Instant endInstantExclusive = end.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant();
        Pageable pageable = PageRequest.of(page, size, sort);
        return noteRepository.findByUserIdAndDeletedFalseAndCreatedAtBetween(
                currentUserId(), startInstant, endInstantExclusive, pageable);
    }

    public List<Note> myLatest(int limit) {
        if (limit <= 0) return List.of();
        Pageable topN = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "createdAt"));
        return noteRepository.findByUserIdAndDeletedFalse(currentUserId(), topN).getContent();
    }

    public Map<EmotionTypes, Long> countMyByEmotion() {
        List<Note> notes = noteRepository.findByUserIdAndDeletedFalse(currentUserId(), Pageable.unpaged()).getContent();
        return notes.stream()
                .filter(n -> n.getCurrentEmotion() != null)
                .collect(Collectors.groupingBy(
                        Note::getCurrentEmotion,
                        () -> new EnumMap<>(EmotionTypes.class),
                        Collectors.counting()
                ));
    }

    public Map<LocalDate, Long> myDailyTotals(LocalDate start, LocalDate end) {
        if (end.isBefore(start)) {
            throw new IllegalArgumentException("Data final não pode ser anterior à inicial");
        }
        Instant startInstant = start.atStartOfDay(ZoneOffset.UTC).toInstant();
        Instant endInstantExclusive = end.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant();

        List<Note> rangeNotes = noteRepository
                .findByUserIdAndDeletedFalseAndCreatedAtBetween(currentUserId(), startInstant, endInstantExclusive, Pageable.unpaged())
                .getContent();

        return rangeNotes.stream()
                .collect(Collectors.groupingBy(
                        n -> LocalDate.ofInstant(n.getCreatedAt(), ZoneOffset.UTC),
                        TreeMap::new,
                        Collectors.counting()
                ));
    }
}
