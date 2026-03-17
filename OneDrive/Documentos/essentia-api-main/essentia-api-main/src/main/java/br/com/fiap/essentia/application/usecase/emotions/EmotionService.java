package br.com.fiap.essentia.application.usecase.emotions;

import br.com.fiap.essentia.application.dto.EmotionRequestDTO;
import br.com.fiap.essentia.domain.enums.EmotionTypes;
import br.com.fiap.essentia.domain.model.Emotion;
import br.com.fiap.essentia.domain.ports.emotions.repository.EmotionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class EmotionService {

    private final EmotionRepository emotionRepository;

    private String currentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null) {
            throw new IllegalStateException("Usuário não autenticado");
        }
        return auth.getName();
    }


    @Transactional(readOnly = true)
    public List<Emotion> getAll() {
        return emotionRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Emotion> getById(String id) {
        return emotionRepository.findById(id).filter(e -> !e.isDeleted());
    }


    @Transactional
    public Emotion create(EmotionRequestDTO dto) {
        Emotion emotion = new Emotion();
        emotion.setEmotionType(dto.getEmotionType());
        emotion.setDayFelt(LocalDateTime.now()); // carimbo automático
        emotion.setUserId(currentUserId());
        emotion.setDeleted(false);
        return emotionRepository.save(emotion);
    }

    @Transactional
    public Emotion update(String id, EmotionRequestDTO dto) {
        return emotionRepository.findById(id)
                .map(emotion -> {
                    if (emotion.isDeleted()) {
                        throw new RuntimeException("Emotion já foi deletada.");
                    }
                    // (opcional) garantir que pertence ao usuário atual
                    if (!Objects.equals(emotion.getUserId(), currentUserId())) {
                        throw new RuntimeException("Você não tem permissão para atualizar este registro.");
                    }
                    emotion.setEmotionType(dto.getEmotionType());
                    emotion.setUserId(currentUserId());
                    return emotionRepository.save(emotion);
                })
                .orElseThrow(() -> new RuntimeException("Emotion não encontrada com id: " + id));
    }

    @Transactional
    public void softDelete(String id) {
        emotionRepository.findById(id).ifPresent(e -> {
            if (!Objects.equals(e.getUserId(), currentUserId())) {
                throw new RuntimeException("Você não tem permissão para remover este registro.");
            }
            e.setDeleted(true);
            emotionRepository.save(e);
        });
    }

    @Transactional
    public Emotion restore(String id) {
        Emotion e = emotionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Emotion não encontrada com id: " + id));
        if (!Objects.equals(e.getUserId(), currentUserId())) {
            throw new RuntimeException("Você não tem permissão para restaurar este registro.");
        }
        if (!e.isDeleted()) return e; // já ativa
        e.setDeleted(false);
        return emotionRepository.save(e);
    }

    @Transactional
    public void hardDelete(String id) {
        Emotion e = emotionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Emotion não encontrada com id: " + id));
        if (!Objects.equals(e.getUserId(), currentUserId())) {
            throw new RuntimeException("Você não tem permissão para excluir este registro.");
        }
        emotionRepository.deleteById(id);
    }

    /* ------------ Consultas “do usuário atual” ------------ */

    @Transactional(readOnly = true)
    public Page<Emotion> getAllPaged(int page, int size, Sort sort) {
        Pageable pageable = PageRequest.of(page, size, sort);
        return emotionRepository.findByDeletedFalse(pageable);
    }

    @Transactional(readOnly = true)
    public Page<Emotion> getMinePaged(int page, int size, Sort sort) {
        Pageable pageable = PageRequest.of(page, size, sort);
        return emotionRepository.findByUserIdAndDeletedFalse(currentUserId(), pageable);
    }

    @Transactional(readOnly = true)
    public Page<Emotion> searchMyByType(EmotionTypes type, int page, int size, Sort sort) {
        Pageable pageable = PageRequest.of(page, size, sort);
        return emotionRepository.findByUserIdAndEmotionTypeAndDeletedFalse(currentUserId(), type, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Emotion> getMyByDateRange(LocalDate startDate, LocalDate endDate, int page, int size, Sort sort) {
        if (endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("Data final não pode ser anterior à inicial");
        }
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime endExclusive = endDate.plusDays(1).atStartOfDay();
        Pageable pageable = PageRequest.of(page, size, sort);
        return emotionRepository.findByUserIdAndDeletedFalseAndDayFeltBetween(
                currentUserId(), start, endExclusive, pageable);
    }

    @Transactional(readOnly = true)
    public List<Emotion> myLatest(int limit) {
        int size = Math.max(0, limit);
        Pageable topN = PageRequest.of(0, size, Sort.by(Sort.Direction.DESC, "dayFelt"));
        return emotionRepository.findByUserIdAndDeletedFalseOrderByDayFeltDesc(currentUserId(), topN).getContent();
    }

    @Transactional(readOnly = true)
    public Map<LocalDate, Long> myDailyTotals(LocalDate startDate, LocalDate endDate) {
        Page<Emotion> page = getMyByDateRange(
                startDate, endDate, 0, Integer.MAX_VALUE, Sort.by("dayFelt").ascending()
        );
        Map<LocalDate, Long> map = new LinkedHashMap<>();
        page.forEach(e -> map.merge(e.getDayFelt().toLocalDate(), 1L, Long::sum));
        return map;
    }

    /* ------------ Checks auxiliares ------------ */

    @Transactional(readOnly = true)
    public boolean belongsToCurrentUser(String id) {
        return emotionRepository.findById(id)
                .filter(e -> !e.isDeleted() && Objects.equals(e.getUserId(), currentUserId()))
                .isPresent();
    }

    @Transactional
    public void softDeleteAsCurrentUser(String id) {
        Emotion e = emotionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Emotion não encontrada com id: " + id));
        if (!Objects.equals(e.getUserId(), currentUserId())) {
            throw new RuntimeException("Você não tem permissão para remover este registro.");
        }
        e.setDeleted(true);
        emotionRepository.save(e);
    }

    @Transactional
    public List<Emotion> bulkCreate(List<EmotionRequestDTO> dtos) {
        String uid = currentUserId();
        LocalDateTime now = LocalDateTime.now();
        List<Emotion> list = new ArrayList<>(dtos.size());
        for (EmotionRequestDTO dto : dtos) {
            Emotion e = new Emotion();
            e.setEmotionType(dto.getEmotionType());
            e.setDayFelt(now);
            e.setUserId(uid);
            e.setDeleted(false);
            list.add(e);
        }
        return emotionRepository.saveAll(list);
    }
}
