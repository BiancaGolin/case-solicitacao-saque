package org.example.solicitacaosaque.controller;

import lombok.AllArgsConstructor;
import org.bson.types.ObjectId;
import org.example.solicitacaosaque.dto.IdSaqueResponse;
import org.example.solicitacaosaque.dto.SaqueResponse;
import org.example.solicitacaosaque.dto.SolicitarSaqueRequest;
import org.example.solicitacaosaque.service.SolicitarSaqueService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
@RequestMapping("/solicitacoes-saque")
public class SolicitarSaqueController {

    private final SolicitarSaqueService service;

    @PostMapping
    public ResponseEntity<?> solicitarSaque(
            @RequestHeader("Idempotency-Key") String idempotencia,
            @RequestBody SolicitarSaqueRequest request
    ) {

        var response = service.solicitarSaque(idempotencia, request);

        return ResponseEntity.status(201).body(response);
    }

    @GetMapping("/{idSolicitacaoSaque}")
    public IdSaqueResponse consultarSaqueId(@PathVariable String idSolicitacaoSaque) {
        return service.consultarSaqueId(idSolicitacaoSaque);
    }
}



