package org.example.solicitacaosaque.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Data
@AllArgsConstructor
public class SolicitarSaqueRequest {

    private String idConta;
    private BigDecimal valor;
    private String canal;
    private String idTerminal;
    private OffsetDateTime dataHoraSolicitacao;
}
