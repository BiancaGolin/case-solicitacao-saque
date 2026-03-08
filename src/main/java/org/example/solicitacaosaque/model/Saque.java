package org.example.solicitacaosaque.model;

import lombok.Data;
import org.bson.types.ObjectId;
import org.example.solicitacaosaque.enums.Canal;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Data
@Document(collection = "saques")
public class Saque {

    @Id
    private ObjectId id;
    private String idConta;
    private BigDecimal valor;
    private OffsetDateTime dataHoraCriacao;
    private Canal canal;
}
