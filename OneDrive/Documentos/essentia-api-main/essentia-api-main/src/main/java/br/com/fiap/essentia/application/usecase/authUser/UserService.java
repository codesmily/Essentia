package br.com.fiap.essentia.application.usecase.authUser;

import br.com.fiap.essentia.domain.model.User;
import br.com.fiap.essentia.domain.ports.auth.repository.UserRepository;
import br.com.fiap.essentia.shared.exception.Exceptions.BusinessException;
import br.com.fiap.essentia.shared.exception.Exceptions.EmailAlreadyInUseException;
import br.com.fiap.essentia.shared.exception.Exceptions.EntityNotFoundException;
import br.com.fiap.essentia.shared.exception.Exceptions.InvalidPasswordException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public User createUser(User user, String plainPassword) {
        requireNonNull(user, "Usuário não pode ser nulo");
        requireNonBlank(user.getEmail(), "Email é obrigatório");
        requireNonBlank(plainPassword, "Senha é obrigatória");
        validatePasswordPolicy(plainPassword);

        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new EmailAlreadyInUseException("Email já cadastrado: " + user.getEmail());
        }

        user.setPasswordHash(passwordEncoder.encode(plainPassword));
        user.setActive(true);
        user.setLastLogin(null);
        return userRepository.save(user);
    }

    public Optional<User> findByEmail(String email) {
        requireNonBlank(email, "Email é obrigatório");
        return userRepository.findByEmail(email);
    }

    public void deactivateUser(String id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado: " + id));

        if (Boolean.FALSE.equals(user.getActive())) {
            throw new BusinessException("Usuário já está inativo: " + id);
        }

        user.setActive(false);
        userRepository.save(user);
    }

    public void updatePassword(String id, String currentPassword, String newPassword) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado: " + id));

        if (!passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
            throw new InvalidPasswordException("Senha atual inválida");
        }

        validatePasswordPolicy(newPassword);
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    public void updateEmail(String id, String newEmail) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Usuário não encontrado: " + id));

        userRepository.findByEmail(newEmail).ifPresent(conflict -> {
            if (!conflict.getId().equals(id)) {
                throw new EmailAlreadyInUseException("Email já em uso: " + newEmail);
            }
        });

        user.setEmail(newEmail);
        userRepository.save(user);
    }

    public void hardDelete(String id) {
        if (userRepository.findById(id).isEmpty()) {
            throw new EntityNotFoundException("Usuário não encontrado: " + id);
        }
        userRepository.deleteById(id);
    }

    /* ============ Helpers ============ */
    private static void requireNonNull(Object o, String msg) {
        if (o == null) throw new IllegalArgumentException(msg);
    }

    private static void requireNonBlank(String s, String msg) {
        if (s == null || s.isBlank()) throw new IllegalArgumentException(msg);
    }

    // Política mínima: 8+ chars, pelo menos 1 letra e 1 dígito.
    private static void validatePasswordPolicy(String pwd) {
        if (pwd.length() < 8 || !pwd.matches(".*[A-Za-z].*") || !pwd.matches(".*\\d.*")) {
            throw new InvalidPasswordException("Senha deve ter ao menos 8 caracteres, letra e número");
        }
    }
}
