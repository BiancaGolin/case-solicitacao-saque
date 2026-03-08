package org.example.solicitacaosaque.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.example.solicitacaosaque.enums.StatusSaque;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Data
@AllArgsConstructor
public class SaqueResponse {
    private String idSolicitacaoSaque;
    private String idConta;
    private BigDecimal valor;
    private String canal;
    private StatusSaque status;
    private BigDecimal saldoAntes;
    private BigDecimal saldoDepois;
    private OffsetDateTime dataHoraCriacao;

}
