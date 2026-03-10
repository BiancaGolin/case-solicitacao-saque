package org.example.solicitacaosaque.service;

import org.example.solicitacaosaque.dto.SaqueResponse;
import org.example.solicitacaosaque.dto.SolicitarSaqueRequest;
import org.example.solicitacaosaque.enums.StatusConta;
import org.example.solicitacaosaque.enums.TipoConta;
import org.example.solicitacaosaque.model.Conta;
import org.example.solicitacaosaque.repository.ContaRepository;
import org.example.solicitacaosaque.repository.SaqueRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

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

    private Conta conta;
    @Autowired
    private SolicitarSaqueService solicitarSaqueService;

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


    @Test
    void testeConcorrenciaApenasUmSaqueConcluido() throws InterruptedException, ExecutionException {

        // cria conta inicial
        Conta conta = new Conta();
        conta.setIdConta("c135");
        conta.setSaldo(new BigDecimal("7000"));
        conta.setStatus(StatusConta.ATIVA);
        conta.setTipoConta(TipoConta.BASICA);
        contaRepository.save(conta);

        contaRepository.save(conta);
        int threads = 5; // 5 requisições simultâneas
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        List<Callable<SaqueResponse>> tasks = new ArrayList<>();

        for (int i = 0; i < threads; i++) {
            final int index = i;
            tasks.add(() -> {
                SolicitarSaqueRequest request = new SolicitarSaqueRequest(
                conta.getIdConta(),
                new BigDecimal("500"),
                "ATM",
                "ATM-TEST-" + index,
                null);// ignora timestamp
                try {
                    return solicitarSaqueService.solicitarSaque("key-" + index, request);
                } catch (ResponseStatusException e) {
                    // Captura concorrência ou outros conflitos
                    return null;
                }
            });
        }

        List<Future<SaqueResponse>> futures = executor.invokeAll(tasks);
        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.SECONDS);

        // Conta quantos saques foram realmente concluídos
        long aprovados = futures.stream().filter(f -> {
            try {
                return f.get() != null;
            } catch (Exception e) {
                return false;
            }
        }).count();

        System.out.println("Saques aprovados: " + aprovados);

        // Verifica que apenas 1 saque foi concluído
        assertEquals(1, aprovados, "Apenas um saque deve ser aprovado em concorrência");

        // Verifica que o saldo foi debitado corretamente
        Conta contaAtualizada = contaRepository.findById(conta.getIdConta()).orElseThrow();
        assertEquals(new BigDecimal("6500.00"), contaAtualizada.getSaldo());

    }
}