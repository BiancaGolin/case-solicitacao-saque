package org.example.solicitacaosaque.service;

import org.example.solicitacaosaque.model.Conta;
import org.example.solicitacaosaque.repository.ContaRepository;
import org.example.solicitacaosaque.repository.SaqueRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
public class SolicitarSaqueServiceTest {

    private static final Logger log = LoggerFactory.getLogger(SolicitarSaqueServiceTest.class);


    @Autowired
    private SolicitarSaqueService saqueService;

    @Autowired
    private ContaRepository contaRepository;

    @Autowired
    private SaqueRepository saqueRepository;

    @BeforeEach
    void limparBanco() {
        contaRepository.deleteAll();
        saqueRepository.deleteAll();
    }

    @Test
    void rollbackSeErroAcontecer() {

        // cria conta inicial
        Conta conta = new Conta();
        conta.setIdConta("c135");
        conta.setSaldo(new BigDecimal("1000"));

        contaRepository.save(conta);

        try {
            saqueService.sacarComErroTest("c135", new BigDecimal("200"));
        } catch (Exception e) {

        }

        Conta contaDepois = contaRepository.findById("c135").orElseThrow();
        log.info("Saldo após rollback: {}", contaDepois.getSaldo());

        // saldo deve continuar igual
        assertEquals(new BigDecimal("1000"), contaDepois.getSaldo());

        // nenhum saque deve ter sido criado
        assertEquals(0, saqueRepository.count());

    }
}
