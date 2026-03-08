package org.example.solicitacaosaque.repository;

import org.bson.types.ObjectId;
import org.example.solicitacaosaque.model.Saque;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;

@Repository
public interface SaqueRepository extends MongoRepository<Saque, ObjectId> {

    List<Saque> findByIdContaAndDataHoraCriacaoAfter(
            String idConta,
            OffsetDateTime inicioDoDia
    );

    List<Saque> findByIdContaAndDataHoraCriacaoBetween(
            String idConta,
            OffsetDateTime dataInicio,
            OffsetDateTime dataFim
    );
}
