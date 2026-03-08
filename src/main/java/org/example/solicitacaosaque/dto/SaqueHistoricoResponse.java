package org.example.solicitacaosaque.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Data
@AllArgsConstructor
public class SaqueHistoricoResponse {

    private String idSolicitacaoSaque;
    private String idConta;
    private BigDecimal valor;
    private OffsetDateTime dataHoraCriacao;

}
