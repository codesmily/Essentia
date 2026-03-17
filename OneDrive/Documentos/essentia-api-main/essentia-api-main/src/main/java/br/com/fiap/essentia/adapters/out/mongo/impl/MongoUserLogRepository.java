package br.com.fiap.essentia.adapters.out.mongo.impl;

import br.com.fiap.essentia.adapters.out.mongo.doc.NoteDocument;
import br.com.fiap.essentia.adapters.out.mongo.doc.UserLogDocument;
import br.com.fiap.essentia.adapters.out.mongo.repo.SpringDataUserLogRepository;
import br.com.fiap.essentia.domain.model.UserLog;
import br.com.fiap.essentia.domain.ports.auth.repository.UserLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class MongoUserLogRepository implements UserLogRepository {

    private final SpringDataUserLogRepository springDataUserLogRepository;

    @Override
    public UserLog save(UserLog userLog) {
        UserLogDocument saved = springDataUserLogRepository.save(UserLogDocument.fromDomain(userLog));
        return saved.toDomain();
    }

    @Override
    public Optional<UserLog> findById(String id) {
        return springDataUserLogRepository.findById(id).map(UserLogDocument::toDomain);
    }

    @Override
    public List<UserLog> findByUserId(String userId) {
        List<UserLogDocument> docs = springDataUserLogRepository.findByUserId(userId);
        return docs.stream()
                .map(UserLogDocument::toDomain)
                .toList();
    }

    @Override
    public List<UserLog> findAll() {
        return springDataUserLogRepository.findAll().stream()
                .map(UserLogDocument::toDomain)
                .toList();
    }

    @Override
    public void deleteById(String id) {
        springDataUserLogRepository.deleteById(id);
    }
}
