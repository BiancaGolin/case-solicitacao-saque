package org.example.solicitacaosaque.controller;

import lombok.AllArgsConstructor;
import org.example.solicitacaosaque.dto.SaqueHistoricoResponse;
import org.example.solicitacaosaque.service.SolicitarHistoricoService;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.List;

@RestController
@AllArgsConstructor
@RequestMapping("/contas")
public class SolicitarHistoricoController {

    private final SolicitarHistoricoService service;

    @GetMapping("{idConta}/solicitacoes-saque")
    public List<SaqueHistoricoResponse> consultarHistorico(
            @PathVariable String idConta,
            @RequestParam String dataInicio,
            @RequestParam String dataFim) {

        OffsetDateTime inicio = OffsetDateTime.parse(dataInicio + "T00:00:00Z");
        OffsetDateTime fim = OffsetDateTime.parse(dataFim + "T23:59:59Z");

        return service.consultarHistoricoSaques(idConta, inicio, fim);
    }
}
