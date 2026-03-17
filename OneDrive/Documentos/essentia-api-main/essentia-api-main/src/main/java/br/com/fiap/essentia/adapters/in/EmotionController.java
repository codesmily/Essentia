package br.com.fiap.essentia.adapters.in;

import br.com.fiap.essentia.application.dto.EmotionRequestDTO;
import br.com.fiap.essentia.application.usecase.emotions.EmotionLogService;
import br.com.fiap.essentia.application.usecase.emotions.EmotionService;
import br.com.fiap.essentia.domain.enums.EmotionTypes;
import br.com.fiap.essentia.domain.model.Emotion;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/emotions")
@RequiredArgsConstructor
@Tag(name = "Emotions", description = "Endpoints para gerenciamento e análise de emoções do usuário")
@SecurityRequirement(name = "bearerAuth")
public class EmotionController {

    private final EmotionService service;
    private final EmotionLogService emotionLogService;

    @Operation(summary = "Listar todas as emoções ativas", description = "Retorna todas as emoções que **não** estão marcadas como deletadas (soft delete).")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Lista retornada com sucesso", content = @Content(schema = @Schema(implementation = Emotion.class)))})
    @GetMapping
    public List<Emotion> getAll() {
        return service.getAll();
    }

    @Operation(summary = "Obter emoção por ID", description = "Busca uma emoção específica pelo seu identificador (somente se não estiver deletada).")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Emoção encontrada", content = @Content(schema = @Schema(implementation = Emotion.class))), @ApiResponse(responseCode = "404", description = "Emoção não encontrada")})
    @GetMapping("/{id}")
    public ResponseEntity<Emotion> getById(@Parameter(description = "ID da emoção", example = "66f2d6c9b6a5b42e4f9c1a23") @PathVariable String id) {
        return service.getById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Criar nova emoção", description = "Cria uma nova emoção associada ao usuário autenticado. O campo `dayFelt` é definido automaticamente.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Emoção criada com sucesso", content = @Content(schema = @Schema(implementation = Emotion.class))), @ApiResponse(responseCode = "400", description = "Payload inválido")})
    @PostMapping
    public Emotion create(@Parameter(description = "Dados para criação da emoção") @Valid @RequestBody EmotionRequestDTO dto, @Parameter(description = "Informações de autenticação do usuário") Authentication authentication) {
        String actorId = authentication.getName();

        Emotion created = service.create(dto);

        emotionLogService.audit("EMOTION_CREATE", created.getUserId(), created.getId(), null, created.getEmotionType() != null ? created.getEmotionType().name() : null, actorId, true, "Emoção criada");
        return created;
    }

    @Operation(summary = "Atualizar emoção por ID", description = "Atualiza os dados de uma emoção existente. Não permite atualizar emoções já deletadas.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Emoção atualizada com sucesso", content = @Content(schema = @Schema(implementation = Emotion.class))), @ApiResponse(responseCode = "404", description = "Emoção não encontrada ou já deletada")})
    @PutMapping("/{id}")
    public ResponseEntity<Emotion> update(@Parameter(description = "ID da emoção a ser atualizada") @PathVariable String id, @Parameter(description = "Dados para atualização") @RequestBody EmotionRequestDTO dto, Authentication authentication) {
        String actorId = authentication.getName();

        return (ResponseEntity<Emotion>) service.getById(id).map(existing -> {
            String oldType = existing.getEmotionType() != null ? existing.getEmotionType().name() : null;
            try {
                Emotion updated = service.update(id, dto);
                String newType = updated.getEmotionType() != null ? updated.getEmotionType().name() : null;

                emotionLogService.audit("EMOTION_UPDATE", updated.getUserId(), updated.getId(), oldType, newType, actorId, true, "Emoção atualizada");
                return ResponseEntity.ok(updated);
            } catch (RuntimeException ex) {
                emotionLogService.audit("EMOTION_UPDATE", existing.getUserId(), existing.getId(), oldType, null, actorId, false, "Falha ao atualizar: " + ex.getMessage());
                return ResponseEntity.notFound().build();
            }
        }).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @Operation(summary = "Remover emoção (soft delete)", description = "Marca a emoção como deletada. O registro permanece no banco para histórico/auditoria.")
    @ApiResponses({@ApiResponse(responseCode = "204", description = "Emoção marcada como deletada"), @ApiResponse(responseCode = "404", description = "Emoção não encontrada")})
    @DeleteMapping("/{id}")
    public ResponseEntity<Object> softDelete(@Parameter(description = "ID da emoção a ser removida") @PathVariable String id, Authentication authentication) {
        String actorId = authentication.getName();

        return service.getById(id).map(existing -> {
            String oldType = existing.getEmotionType() != null ? existing.getEmotionType().name() : null;
            try {
                service.softDelete(id);
                emotionLogService.audit("EMOTION_SOFT_DELETE", existing.getUserId(), existing.getId(), oldType, null, actorId, true, "Soft delete aplicado");
                return ResponseEntity.noContent().build();
            } catch (RuntimeException ex) {
                emotionLogService.audit("EMOTION_SOFT_DELETE", existing.getUserId(), existing.getId(), oldType, null, actorId, false, "Falha no soft delete: " + ex.getMessage());
                return ResponseEntity.notFound().build();
            }
        }).orElseGet(() -> ResponseEntity.notFound().build());
    }

    /* ===== Rotas “minhas” (usam userId do JWT no service) ===== */

    @Operation(summary = "Minhas emoções (paginado)", description = "Retorna emoções ativas do **usuário autenticado**, com paginação e ordenação.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Página retornada com sucesso")})
    @GetMapping("/me/paged")
    public Page<Emotion> getMinePaged(@Parameter(description = "Número da página (0-base)", example = "0") @RequestParam(defaultValue = "0") int page, @Parameter(description = "Tamanho da página", example = "10") @RequestParam(defaultValue = "10") int size, @Parameter(description = "Campo de ordenação", example = "dayFelt") @RequestParam(defaultValue = "dayFelt") String sortBy, @Parameter(description = "Direção da ordenação (ASC/DESC)", example = "DESC") @RequestParam(defaultValue = "DESC") String direction) {
        Sort sort = Sort.by(Sort.Direction.fromString(direction), sortBy);
        return service.getMinePaged(page, size, sort);
    }

    @Operation(summary = "Minhas emoções por tipo (paginado)", description = "Filtra emoções ativas do **usuário autenticado** por `EmotionTypes`.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Página retornada com sucesso")})
    @GetMapping("/me/type/{type}")
    public Page<Emotion> getMyByType(@Parameter(description = "Tipo de emoção", schema = @Schema(implementation = EmotionTypes.class)) @PathVariable EmotionTypes type, @Parameter(description = "Número da página (0-base)", example = "0") @RequestParam(defaultValue = "0") int page, @Parameter(description = "Tamanho da página", example = "10") @RequestParam(defaultValue = "10") int size, @Parameter(description = "Campo de ordenação", example = "dayFelt") @RequestParam(defaultValue = "dayFelt") String sortBy, @Parameter(description = "Direção da ordenação (ASC/DESC)", example = "DESC") @RequestParam(defaultValue = "DESC") String direction) {
        Sort sort = Sort.by(Sort.Direction.fromString(direction), sortBy);
        return service.searchMyByType(type, page, size, sort);
    }

    @Operation(summary = "Minhas emoções por período (paginado)", description = "Filtra emoções ativas do **usuário autenticado** por intervalo de datas (`yyyy-MM-dd`).")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Página retornada com sucesso"), @ApiResponse(responseCode = "400", description = "Parâmetros de data inválidos")})
    @GetMapping("/me/period")
    public Page<Emotion> getMyByDateRange(@Parameter(description = "Data inicial (yyyy-MM-dd)", example = "2025-01-01") @RequestParam String startDate, @Parameter(description = "Data final (yyyy-MM-dd)", example = "2025-12-31") @RequestParam String endDate, @Parameter(description = "Número da página (0-base)", example = "0") @RequestParam(defaultValue = "0") int page, @Parameter(description = "Tamanho da página", example = "10") @RequestParam(defaultValue = "10") int size, @Parameter(description = "Campo de ordenação", example = "dayFelt") @RequestParam(defaultValue = "dayFelt") String sortBy, @Parameter(description = "Direção da ordenação (ASC/DESC)", example = "DESC") @RequestParam(defaultValue = "DESC") String direction) {
        Sort sort = Sort.by(Sort.Direction.fromString(direction), sortBy);
        return service.getMyByDateRange(LocalDate.parse(startDate), LocalDate.parse(endDate), page, size, sort);
    }

    @Operation(summary = "Minhas últimas N emoções", description = "Retorna os **N** registros mais recentes (`dayFelt` desc) do **usuário autenticado**.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Lista retornada com sucesso")})
    @GetMapping("/me/latest")
    public List<Emotion> myLatest(@Parameter(description = "Quantidade de registros recentes", example = "5") @RequestParam(defaultValue = "5") int limit) {
        return service.myLatest(limit);
    }

    /* -------- Restore / Hard delete (iguais, pois usam o id do registro) -------- */

    @Operation(summary = "Restaurar (undelete) uma emoção", description = "Remove a marcação de soft delete de uma emoção.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Emoção restaurada", content = @Content(schema = @Schema(implementation = Emotion.class))), @ApiResponse(responseCode = "404", description = "Emoção não encontrada")})
    @PatchMapping("/{id}/restore")
    public ResponseEntity<Emotion> restore(@Parameter(description = "ID da emoção") @PathVariable String id, Authentication authentication) {
        String actorId = authentication.getName();

        EmotionTypes oldType = service.getById(id).map(Emotion::getEmotionType).orElse(null);

        try {
            Emotion restored = service.restore(id);
            emotionLogService.audit("EMOTION_RESTORE", restored.getUserId(), restored.getId(), oldType != null ? oldType.name() : null, restored.getEmotionType() != null ? restored.getEmotionType().name() : null, actorId, true, "Emoção restaurada");
            return ResponseEntity.ok(restored);
        } catch (RuntimeException ex) {
            emotionLogService.audit("EMOTION_RESTORE", null, id, oldType != null ? oldType.name() : null, null, actorId, false, "Falha ao restaurar: " + ex.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Remoção definitiva (hard delete)", description = "Exclui o registro de forma **irreversível**.")
    @ApiResponses({@ApiResponse(responseCode = "204", description = "Emoção removida definitivamente"), @ApiResponse(responseCode = "404", description = "Emoção não encontrada")})
    @DeleteMapping("/{id}/hard")
    public ResponseEntity<Object> hardDelete(@Parameter(description = "ID da emoção") @PathVariable String id, Authentication authentication) {
        String actorId = authentication.getName();

        return service.getById(id).map(existing -> {
            String oldType = existing.getEmotionType() != null ? existing.getEmotionType().name() : null;
            try {
                service.hardDelete(id);
                emotionLogService.audit("EMOTION_HARD_DELETE", existing.getUserId(), existing.getId(), oldType, null, actorId, true, "Hard delete aplicado");
                return ResponseEntity.noContent().build();
            } catch (RuntimeException ex) {
                emotionLogService.audit("EMOTION_HARD_DELETE", existing.getUserId(), existing.getId(), oldType, null, actorId, false, "Falha no hard delete: " + ex.getMessage());
                return ResponseEntity.notFound().build();
            }
        }).orElseGet(() -> ResponseEntity.notFound().build());
    }
}
