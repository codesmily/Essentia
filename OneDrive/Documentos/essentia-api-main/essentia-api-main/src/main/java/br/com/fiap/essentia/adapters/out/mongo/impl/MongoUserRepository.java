package br.com.fiap.essentia.adapters.out.mongo.impl;

import br.com.fiap.essentia.adapters.out.mongo.doc.UserDocument;
import br.com.fiap.essentia.adapters.out.mongo.repo.SpringDataUserRepository;
import br.com.fiap.essentia.domain.model.User;
import br.com.fiap.essentia.domain.ports.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class MongoUserRepository implements UserRepository {

    private final SpringDataUserRepository springDataUserRepository;

    @Override
    public Optional<User> findById(String id) {
        return springDataUserRepository.findById(id).map(UserDocument::toDomain);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return springDataUserRepository.findByEmail(email).map(UserDocument::toDomain);
    }

    @Override
    public Optional<User> findByUsername(String name) {
        return springDataUserRepository.findByName(name).map(UserDocument::toDomain);
    }

    @Override
    public User save(User user) {
        UserDocument doc = UserDocument.fromDomain(user);
        UserDocument saved = springDataUserRepository.save(doc);
        return saved.toDomain();
    }

    @Override
    public void deleteById(String id) {
        springDataUserRepository.deleteById(id);
    }

    @Override
    public boolean existsByEmail(String emailTemp) {
        return springDataUserRepository.existsByEmail(emailTemp);
    }

    @Override
    public void deleteByEmail(String emailTemp) {
        springDataUserRepository.deleteByEmail(emailTemp);
    }
}
