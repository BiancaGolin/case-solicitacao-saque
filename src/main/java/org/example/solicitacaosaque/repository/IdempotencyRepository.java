package org.example.solicitacaosaque.repository;

import org.example.solicitacaosaque.model.Idempotency;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface IdempotencyRepository extends MongoRepository<Idempotency, String> {
    Optional<Idempotency> findByIdempotencyKey(String key);
}
