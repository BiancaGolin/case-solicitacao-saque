package org.example.solicitacaosaque.repository;

import org.example.solicitacaosaque.model.Conta;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ContaRepository extends MongoRepository<Conta, String> {
}
