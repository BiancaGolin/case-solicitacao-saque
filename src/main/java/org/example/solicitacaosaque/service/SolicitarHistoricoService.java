package org.example.solicitacaosaque.service;

import lombok.RequiredArgsConstructor;
import org.example.solicitacaosaque.dto.SaqueHistoricoResponse;
import org.example.solicitacaosaque.model.Saque;
import org.example.solicitacaosaque.repository.SaqueRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class SolicitarHistoricoService {

    private final SaqueRepository saqueRepository;
    private static final Logger log = LoggerFactory.getLogger(SolicitarSaqueService.class);

    public List<SaqueHistoricoResponse> consultarHistoricoSaques(
            String idConta, OffsetDateTime dataInicio, OffsetDateTime dataFim) {

        log.debug(
                "event=consulta_historico_inicio conta_id={} data_inicio={} data_fim={}",
                idConta,
                dataInicio,
                dataFim
        );

        List<Saque> saques = saqueRepository.findByIdContaAndDataHoraCriacaoBetween(
                idConta, dataInicio, dataFim);

        log.info(
                "event=consulta_historico_resultado conta_id={} quantidade_saques={}",
                idConta,
                saques.size()
        );

        // Transformar cada Saque em DTO de resposta
        return saques.stream()
                .map(this::montarSaqueHistoricoResponse)
                .toList();
    }

    // Metodo para montar o DTO
    private SaqueHistoricoResponse montarSaqueHistoricoResponse(Saque saque) {

        log.debug(
                "event=montando_historico_saque saque_id={} conta_id={} valor={}",
                saque.getId(),
                saque.getIdConta(),
                saque.getValor()
        );

        return new SaqueHistoricoResponse(
                saque.getId().toHexString(),
                saque.getIdConta(),
                saque.getValor(),
                saque.getDataHoraCriacao()
        );
    }
}
