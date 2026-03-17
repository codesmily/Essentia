package br.com.fiap.essentia.adapters.out.mongo.repo;

import br.com.fiap.essentia.adapters.out.mongo.doc.UserLogDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface SpringDataUserLogRepository extends MongoRepository<UserLogDocument, String> {
    List<UserLogDocument> findByUserId(String userId);
}
