package br.com.fiap.essentia.application.usecase.authUser;

import br.com.fiap.essentia.adapters.out.jwt.JwtTokenProvider;
import br.com.fiap.essentia.application.dto.AuthResponseDto;
import br.com.fiap.essentia.application.dto.LoginRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.logging.Level;
import java.util.logging.Logger;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider; // seu provider atual
    private final Logger logger = Logger.getLogger(AuthService.class.getName());

    // pega do application.yml: app.jwt.expiration-ms: 3600000
    @Value("${app.jwt.expiration-ms:3600000}")
    private long accessExpirationMs;

    public AuthResponseDto authenticate(LoginRequestDto dto) {
        final String email = dto.getEmail();

        var userOpt = userService.findByEmail(email);
        var user = userOpt.orElseThrow(() -> {
            logger.log(Level.WARNING, "Login falhou - usuário não encontrado: {0}", email);
            return new BadCredentialsException("Credenciais inválidas");
        });

        if (!passwordEncoder.matches(dto.getPassword(), user.getPasswordHash())) {
            logger.log(Level.WARNING, "Login falhou - senha inválida para: {0}", email);
            throw new BadCredentialsException("Credenciais inválidas");
        }

        if (Boolean.FALSE.equals(user.getActive())) {
            logger.log(Level.WARNING, "Login falhou - conta desativada: {0}", email);
            throw new BadCredentialsException("Conta desativada");
        }
        String token = tokenProvider.createToken(user.getId());


        return new AuthResponseDto(token, "Bearer", (int) accessExpirationMs, user.getId(), user.getName());
    }
}
