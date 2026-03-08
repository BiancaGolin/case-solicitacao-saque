package org.example.solicitacaosaque.service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Service;

@Service
public class SaqueMetricas {

    private final Counter aprovadas;
    private final Counter rejeitadas;
    private final Timer tempoResposta;

    public SaqueMetricas(MeterRegistry registry) {
        this.aprovadas = Counter.builder("saque.aprovadas")
                .description("Quantidade de solicitações de saque aprovadas")
                .register(registry);

        this.rejeitadas = Counter.builder("saque.rejeitadas")
                .description("Quantidade de solicitações de saque rejeitadas")
                .register(registry);

        this.tempoResposta = Timer.builder("saque.tempo.resposta")
                .description("Tempo de resposta do endpoint de solicitação de saque")
                .register(registry);
    }

    public void incrementarAprovadas() {
        aprovadas.increment();
    }

    public void incrementarRejeitadas() {
        rejeitadas.increment();
    }

    public Timer getTimer() {
        return tempoResposta;
    }
}
