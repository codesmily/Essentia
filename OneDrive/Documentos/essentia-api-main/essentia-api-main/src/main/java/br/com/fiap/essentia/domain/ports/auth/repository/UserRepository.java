package br.com.fiap.essentia.domain.ports.auth.repository;

import br.com.fiap.essentia.domain.model.User;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface UserRepository {
    Optional<User> findById(String id);
    Optional<User> findByEmail(String email);
    Optional<User> findByUsername(String username);
    User save(User user);
    void deleteById(String id);
    boolean existsByEmail(String emailTemp);
    void deleteByEmail(String emailTemp);
}
