package org.example.solicitacaosaque.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Data
@AllArgsConstructor
public class IdSaqueResponse {

    private String idSolicitacaoSaque;
    private String idConta;
    private BigDecimal valor;
    private OffsetDateTime dataHoraCriacao;
}
