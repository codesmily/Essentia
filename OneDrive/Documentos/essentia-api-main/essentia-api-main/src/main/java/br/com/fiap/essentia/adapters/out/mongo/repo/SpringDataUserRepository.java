package br.com.fiap.essentia.adapters.out.mongo.repo;

import br.com.fiap.essentia.adapters.out.mongo.doc.UserDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface SpringDataUserRepository extends MongoRepository<UserDocument, String> {
    Optional<UserDocument> findByEmail(String email);
    Optional<UserDocument> findByName(String name);
    boolean existsByEmail(String email);
    void deleteByEmail(String email);
}
