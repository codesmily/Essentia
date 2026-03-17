package br.com.fiap.essentia.domain.ports.auth.repository;

import br.com.fiap.essentia.domain.model.UserLog;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface UserLogRepository {
    UserLog save(UserLog userLog);
    Optional<UserLog> findById(String id);
    List<UserLog> findByUserId(String userId);
    List<UserLog> findAll();
    void deleteById(String id);
}