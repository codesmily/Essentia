package br.com.fiap.essentia.adapters.in;

import br.com.fiap.essentia.application.dto.NoteRequestDTO;
import br.com.fiap.essentia.application.usecase.notes.NoteService;
import br.com.fiap.essentia.application.usecase.notes.NotesLogService;
import br.com.fiap.essentia.domain.enums.EmotionTypes;
import br.com.fiap.essentia.domain.model.Note;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/notes")
@RequiredArgsConstructor
@Tag(name = "Notes", description = "Gerenciamento de notas (CRUD, filtros, estatísticas e soft delete)")
@SecurityRequirement(name = "bearerAuth")
public class NotesController {

    private final NoteService noteService;
    private final NotesLogService noteLogService;

    @Operation(summary = "Listar todas as notas")
    @ApiResponse(responseCode = "200", description = "Lista retornada", content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = Note.class))))
    @GetMapping
    public List<Note> getAll() {
        return noteService.getAll();
    }

    @Operation(summary = "Buscar nota por ID", description = "Retorna uma nota pelo ID, se não estiver deletada.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Nota encontrada", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Note.class))), @ApiResponse(responseCode = "404", description = "Nota não encontrada")})
    @GetMapping("/{id}")
    public ResponseEntity<Note> getById(@Parameter(description = "ID da nota") @PathVariable String id) {
        return noteService.getById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Criar nova nota", description = "Cria uma nova nota associada ao usuário autenticado.")
    @ApiResponses({@ApiResponse(responseCode = "201", description = "Nota criada com sucesso", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Note.class))), @ApiResponse(responseCode = "400", description = "Payload inválido")})
    @PostMapping
    public ResponseEntity<Note> create(@Parameter(description = "Dados da nota a ser criada") @Valid @RequestBody NoteRequestDTO dto, Authentication authentication) {

        final String action = "NOTES_CREATE";
        final String actorId = authentication != null ? authentication.getName() : null;

        try {
            Note created = noteService.create(dto); // Service pega userId do JWT
            noteLogService.audit(action, created.getId(), actorId, true, "Nota criada");
            return ResponseEntity.status(HttpStatus.CREATED).body(created);
        } catch (RuntimeException ex) {
            noteLogService.audit(action, null, actorId, false, "Falha ao criar nota: " + ex.getMessage());
            throw ex;
        }
    }

    @Operation(summary = "Atualizar nota", description = "Atualiza título e conteúdo de uma nota existente.")
    @ApiResponses({@ApiResponse(responseCode = "200", description = "Nota atualizada", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Note.class))), @ApiResponse(responseCode = "404", description = "Nota não encontrada ou já deletada")})
    @PutMapping("/{id}")
    public ResponseEntity<Note> update(@Parameter(description = "ID da nota") @PathVariable String id, @RequestBody NoteRequestDTO dto, Authentication authentication) {

        final String action = "NOTES_UPDATE";
        final String actorId = authentication != null ? authentication.getName() : null;

        try {
            Note updated = noteService.update(id, dto);
            noteLogService.audit(action, id, actorId, true, "Nota atualizada");
            return ResponseEntity.ok(updated);
        } catch (RuntimeException ex) {
            noteLogService.audit(action, id, actorId, false, "Falha ao atualizar: " + ex.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Deletar nota (soft delete)", description = "Marca a nota como deletada.")
    @ApiResponses({@ApiResponse(responseCode = "204", description = "Nota deletada"), @ApiResponse(responseCode = "404", description = "Nota não encontrada")})
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> softDelete(@Parameter(description = "ID da nota") @PathVariable String id, Authentication authentication) {

        final String action = "NOTES_SOFT_DELETE";
        final String actorId = authentication != null ? authentication.getName() : null;

        try {
            noteService.softDelete(id);
            noteLogService.audit(action, id, actorId, true, "Nota marcada como deletada");
            return ResponseEntity.noContent().build();
        } catch (RuntimeException ex) {
            noteLogService.audit(action, id, actorId, false, "Falha ao deletar (soft): " + ex.getMessage());
            throw ex;
        }
    }

    /* ===== Rotas “minhas” (usam o userId do JWT no Service) ===== */

    @Operation(summary = "Minhas notas (paginado)", description = "Notas ativas do usuário autenticado.")
    @GetMapping("/me/paged")
    public Page<Note> getMinePaged(@Parameter(description = "Número da página (0-base)", example = "0") @RequestParam(defaultValue = "0") int page, @Parameter(description = "Tamanho da página", example = "10") @RequestParam(defaultValue = "10") int size, @Parameter(description = "Campo de ordenação", example = "createdAt") @RequestParam(defaultValue = "createdAt") String sortBy, @Parameter(description = "Direção da ordenação (ASC/DESC)", example = "DESC") @RequestParam(defaultValue = "DESC") String direction) {
        return noteService.getMinePaged(page, size, Sort.by(Sort.Direction.fromString(direction), sortBy));
    }

    @Operation(summary = "Minhas notas por emoção (paginado)", description = "Filtra pelas notas com `currentEmotion`.")
    @GetMapping("/me/emotion/{emotion}")
    public Page<Note> getMyByEmotion(@Parameter(description = "Emoção atual", schema = @Schema(implementation = EmotionTypes.class)) @PathVariable EmotionTypes emotion, @Parameter(description = "Número da página (0-base)", example = "0") @RequestParam(defaultValue = "0") int page, @Parameter(description = "Tamanho da página", example = "10") @RequestParam(defaultValue = "10") int size, @Parameter(description = "Campo de ordenação", example = "createdAt") @RequestParam(defaultValue = "createdAt") String sortBy, @Parameter(description = "Direção da ordenação (ASC/DESC)", example = "DESC") @RequestParam(defaultValue = "DESC") String direction) {
        return noteService.searchMyByEmotion(emotion, page, size, Sort.by(Sort.Direction.fromString(direction), sortBy));
    }

    @Operation(summary = "Minhas notas por período (paginado)", description = "Filtra por intervalo de datas (`yyyy-MM-dd`).")
    @GetMapping("/me/period")
    public Page<Note> getMyByDateRange(@Parameter(description = "Data inicial (yyyy-MM-dd)", example = "2025-01-01") @RequestParam String startDate, @Parameter(description = "Data final (yyyy-MM-dd)", example = "2025-12-31") @RequestParam String endDate, @Parameter(description = "Número da página (0-base)", example = "0") @RequestParam(defaultValue = "0") int page, @Parameter(description = "Tamanho da página", example = "10") @RequestParam(defaultValue = "10") int size, @Parameter(description = "Campo de ordenação", example = "createdAt") @RequestParam(defaultValue = "createdAt") String sortBy, @Parameter(description = "Direção da ordenação (ASC/DESC)", example = "DESC") @RequestParam(defaultValue = "DESC") String direction) {
        return noteService.getMyByDateRange(LocalDate.parse(startDate), LocalDate.parse(endDate), page, size, Sort.by(Sort.Direction.fromString(direction), sortBy));
    }

    @Operation(summary = "Minhas últimas N notas", description = "Mais recentes por `createdAt`.")
    @GetMapping("/me/latest")
    public List<Note> myLatest(@Parameter(description = "Quantidade de registros", example = "5") @RequestParam(defaultValue = "5") int limit) {
        return noteService.myLatest(limit);
    }

    @Operation(summary = "Minhas notas: contagem por emoção", description = "`EmotionTypes → total`.")
    @GetMapping("/me/countByEmotion")
    public Map<EmotionTypes, Long> countMyByEmotion() {
        return noteService.countMyByEmotion();
    }

    @Operation(summary = "Minhas notas: totais diários por período", description = "`data → total`.")
    @GetMapping("/me/dailyTotals")
    public Map<LocalDate, Long> myDailyTotals(@Parameter(description = "Data inicial (yyyy-MM-dd)", example = "2025-01-01") @RequestParam String startDate, @Parameter(description = "Data final (yyyy-MM-dd)", example = "2025-12-31") @RequestParam String endDate) {
        return noteService.myDailyTotals(LocalDate.parse(startDate), LocalDate.parse(endDate));
    }

    /* ===== Restore / Hard delete ===== */

    @Operation(summary = "Restaurar nota deletada")
    @PatchMapping("/{id}/restore")
    public ResponseEntity<Note> restore(@Parameter(description = "ID da nota") @PathVariable String id, Authentication authentication) {
        final String action = "NOTES_RESTORE";
        final String actorId = authentication != null ? authentication.getName() : null;

        try {
            Note restored = noteService.restore(id);
            noteLogService.audit(action, id, actorId, true, "Nota restaurada");
            return ResponseEntity.ok(restored);
        } catch (RuntimeException ex) {
            noteLogService.audit(action, id, actorId, false, "Falha ao restaurar: " + ex.getMessage());
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Remoção definitiva (hard delete)")
    @DeleteMapping("/{id}/hard")
    public ResponseEntity<Void> hardDelete(@Parameter(description = "ID da nota") @PathVariable String id, Authentication authentication) {
        final String action = "NOTES_HARD_DELETE";
        final String actorId = authentication != null ? authentication.getName() : null;

        try {
            noteService.hardDelete(id);
            noteLogService.audit(action, id, actorId, true, "Nota removida definitivamente");
            return ResponseEntity.noContent().build();
        } catch (RuntimeException ex) {
            noteLogService.audit(action, id, actorId, false, "Falha no hard delete: " + ex.getMessage());
            throw ex;
        }
    }
}
