package br.com.fiap.essentia.adapters.in;

import br.com.fiap.essentia.application.dto.AuthResponseDto;
import br.com.fiap.essentia.application.dto.LoginRequestDto;
import br.com.fiap.essentia.application.dto.UserCreateRequestDto;
import br.com.fiap.essentia.application.usecase.authUser.AuthService;
import br.com.fiap.essentia.application.usecase.authUser.UserLogService;
import br.com.fiap.essentia.application.usecase.authUser.UserService;
import br.com.fiap.essentia.domain.model.User;
import br.com.fiap.essentia.shared.exception.Exceptions;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "Endpoints de autenticação e gerenciamento de usuários")
public class AuthController {
    private final UserLogService userLogService;
    private final AuthService authService;
    private final UserService userService;
    private final Logger logger = Logger.getLogger(AuthController.class.getName());

    @Operation(
            summary = "Login com e-mail e senha",
            description = "Autentica o usuário e retorna o token JWT (Bearer). " +
                    "Este endpoint **não** exige autenticação prévia."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Autenticado com sucesso",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = AuthResponseDto.class))),
            @ApiResponse(responseCode = "400", description = "Erro de validação dos dados de entrada",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = br.com.fiap.essentia.shared.exception.GlobalExceptionHandler.ApiError.class))),
            @ApiResponse(responseCode = "401", description = "Credenciais inválidas",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = br.com.fiap.essentia.shared.exception.GlobalExceptionHandler.ApiError.class))),
            @ApiResponse(responseCode = "500", description = "Erro interno",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = br.com.fiap.essentia.shared.exception.GlobalExceptionHandler.ApiError.class)))
    })
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody @Valid LoginRequestDto dto) {
        final String action = "AUTH_LOGIN";
        try {
            AuthResponseDto res = authService.authenticate(dto);
            userLogService.audit(action, res.getUserId(), dto.getEmail(), true, "Login bem-sucedido");
            return ResponseEntity.ok(res);
        } catch (BadCredentialsException ex) {
            userLogService.audit(action, null, dto.getEmail(), false, "Credenciais inválidas");
            logger.log(Level.WARNING, "Falha de autenticação: " + dto.getEmail(), ex);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    new br.com.fiap.essentia.shared.exception.GlobalExceptionHandler.ApiError(
                            java.time.OffsetDateTime.now(), 401, "Unauthorized", "Credenciais inválidas", "/auth/login", null
                    )
            );
        } catch (Exception ex) {
            userLogService.audit(action, null, dto.getEmail(), false, "Erro inesperado no login: " + ex.getMessage());
            throw ex; // deixa o @ControllerAdvice cuidar
        }
    }

    @Operation(
            summary = "Registro de novo usuário",
            description = "Cria um novo usuário ativo no sistema. " +
                    "Este endpoint **não** exige autenticação prévia."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Usuário criado",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = HashMap.class))),
            @ApiResponse(responseCode = "400", description = "Senha inválida / dados inválidos",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = br.com.fiap.essentia.shared.exception.GlobalExceptionHandler.ApiError.class))),
            @ApiResponse(responseCode = "409", description = "E-mail já cadastrado",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = br.com.fiap.essentia.shared.exception.GlobalExceptionHandler.ApiError.class)))
    })
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody @Valid UserCreateRequestDto dto) {
        final String action = "AUTH_REGISTER";
        try {
            User user = new User();
            user.setName(dto.getName());
            user.setEmail(dto.getEmail());

            var created = userService.createUser(user, dto.getPassword());

            var response = new HashMap<String, Object>();
            response.put("id", created.getId());
            response.put("name", created.getName());
            response.put("email", created.getEmail());
            response.put("active", created.getActive());

            userLogService.audit(action, created.getId(), created.getEmail(), true, "Usuário criado com sucesso");
            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (Exceptions.EmailAlreadyInUseException | Exceptions.InvalidPasswordException | IllegalArgumentException ex) {
            userLogService.audit(action, null, dto.getEmail(), false, "Erro de dados no registro: " + ex.getMessage());
            throw ex;
        } catch (Exception ex) {
            userLogService.audit(action, null, dto.getEmail(), false, "Erro inesperado no registro: " + ex.getMessage());
            throw ex;
        }
    }

    @Operation(
            summary = "Desativar usuário",
            description = "Desativa (inativa) um usuário. Permitido ao próprio usuário (mesmo id) ou a ADMIN.",
            security = {@SecurityRequirement(name = "bearerAuth")}
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Usuário desativado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Sem permissão",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = br.com.fiap.essentia.shared.exception.GlobalExceptionHandler.ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = br.com.fiap.essentia.shared.exception.GlobalExceptionHandler.ApiError.class)))
    })
    @DeleteMapping("/users/{id}/deactivate")
    @PreAuthorize("#id == authentication.name or hasRole('ADMIN')")
    public ResponseEntity<Void> deactivate(@PathVariable String id) {
        final String action = "AUTH_DEACTIVATE";
        try {
            userService.deactivateUser(id);
            userLogService.audit(action, id, null, true, "Usuário inativado com sucesso");
            return ResponseEntity.noContent().build();
        } catch (Exceptions.EntityNotFoundException | Exceptions.BusinessException | AccessDeniedException ex) {
            userLogService.audit(action, id, null, false, "Falha ao inativar usuário: " + ex.getMessage());
            throw ex;
        } catch (Exception ex) {
            userLogService.audit(action, id, null, false, "Erro inesperado ao inativar: " + ex.getMessage());
            throw ex;
        }
    }
}
