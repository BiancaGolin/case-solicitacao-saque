package org.example.solicitacaosaque.service;

import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.example.solicitacaosaque.dto.IdSaqueResponse;
import org.example.solicitacaosaque.dto.SaqueResponse;
import org.example.solicitacaosaque.dto.SolicitarSaqueRequest;
import org.example.solicitacaosaque.enums.Canal;
import org.example.solicitacaosaque.enums.StatusConta;
import org.example.solicitacaosaque.enums.StatusSaque;
import org.example.solicitacaosaque.enums.TipoConta;
import org.example.solicitacaosaque.model.Conta;
import org.example.solicitacaosaque.model.Idempotency;
import org.example.solicitacaosaque.model.Saque;
import org.example.solicitacaosaque.repository.ContaRepository;
import org.example.solicitacaosaque.repository.IdempotencyRepository;
import org.example.solicitacaosaque.repository.SaqueRepository;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.dao.OptimisticLockingFailureException;
import java.util.UUID;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
//@Transactional
public class SolicitarSaqueService {

    private final ContaRepository contaRepository;
    private final SaqueRepository saqueRepository;
    private final IdempotencyRepository idempotencyRepository;

    private static final Logger log = LoggerFactory.getLogger(SolicitarSaqueService.class);
    private final SaqueMetricas saqueMetricas;


    public SaqueResponse solicitarSaque(String idempotencia, SolicitarSaqueRequest request) {

        return saqueMetricas.getTimer().record(() -> {

            try {
                log.info(
                        "event=solicitacao_saque_recebida conta_id={} valor={} canal={} idempotencia={}",
                        request.getIdConta(),
                        request.getValor(),
                        request.getCanal(),
                        idempotencia
                );

                //Verifica idempotencia
                Optional<Idempotency> registro =
                        idempotencyRepository.findByIdempotencyKey(idempotencia);

                if (registro.isPresent()) {
                    log.info("event=idempotency_replay key={}", idempotencia);
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "Requisição duplicada");
                }

                //Regras de negocio
                Conta conta = validarContaAtiva(request.getIdConta());
                validarSaldoDisponivel(conta, request.getValor());
                validarLimiteSaque(conta, request.getValor());
                validarLimiteDiario(conta, request.getValor());
                validarLimiteCanal(Canal.valueOf(request.getCanal().toUpperCase()), request.getValor());

                //Criar e persistir o saque
                Saque novoSaque = criarESalvarSaque(conta, request.getValor());

                //Gerar resposta
                SaqueResponse response = montarSaqueResponse(conta, novoSaque, request.getCanal());

                //Salvar idempotencia
                Idempotency record = new Idempotency();
                record.setIdempotencyKey(idempotencia);
                record.setPayloadHash("hash");
                record.setResponseJson("json");
                record.setCreatedAt(OffsetDateTime.now());

                idempotencyRepository.save(record);

                saqueMetricas.incrementarAprovadas();
                //Montar DTO de resposta
                return response;

            } catch (OptimisticLockingFailureException e) {
                log.warn(
                        "event=concorrencia_detectada conta_id={} valor={}",
                        request.getIdConta(),
                        request.getValor(),
                        e
                );

                saqueMetricas.incrementarRejeitadas();

                throw new ResponseStatusException(
                        HttpStatus.CONFLICT,
                        "Operação concorrente detectada. Tente novamente."
                );
            } catch (ResponseStatusException e) {
                // Captura casos como "Requisição duplicada"
                log.warn("event=erro_negocio conta_id={} valor={} status={} message={}",
                        request.getIdConta(), request.getValor(), e.getStatusCode(), e.getReason());
                saqueMetricas.incrementarRejeitadas();
                throw e; // relança a exceção para o Spring tratar

            } catch (Exception e) {
                // Captura qualquer outro erro inesperado
                log.error("event=erro_inesperado conta_id={} valor={}", request.getIdConta(), request.getValor(), e);
                saqueMetricas.incrementarRejeitadas();
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Erro inesperado");
            }
        });
    }


    private Conta validarContaAtiva(String idConta) {

        log.debug("event=validacao_conta_inicio conta_id={}", idConta);

        Conta conta = contaRepository.findById(idConta)
                .orElseThrow(() -> {
                    log.warn("event=conta_nao_encontrada conta_id={}", idConta);
                    return new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            "Conta não localizada");
                });

        log.debug(
                "event=validacao_conta conta_id={} status={}",
                conta.getIdConta(),
                conta.getStatus()
        );

        if (conta.getStatus() != StatusConta.ATIVA) {

            log.warn(
                    "event=conta_inativa conta_id={} status={}",
                    idConta,
                    conta.getStatus()
            );

            throw new ResponseStatusException(
                    HttpStatus.UNPROCESSABLE_ENTITY,
                    "Conta está ENCERRADA");
        }

        log.debug("event=conta_validada conta_id={}", idConta);

        return conta;
    }

    private void validarSaldoDisponivel(Conta conta, BigDecimal valorSaque) {

        BigDecimal saldoConta = conta.getSaldo();

        log.debug(
                "event=validacao_saldo conta_id={} saldo_atual={} valor_saque={}",
                conta.getIdConta(),
                saldoConta,
                valorSaque
        );

        //se o saldoConta for menor que 0, significa que eu não tenho dinheiro em conta suficiente pra fazer essa operação
        if (saldoConta.compareTo(valorSaque) < 0) {

            log.warn(
                    "event=saldo_insuficiente conta_id={} saldo_atual={} valor_saque={}",
                    conta.getIdConta(),
                    saldoConta,
                    valorSaque
            );

            throw new ResponseStatusException(
                    HttpStatus.UNPROCESSABLE_ENTITY,
                    "Saldo insuficiente para realizar o saque"
            );
        }
    }

    private BigDecimal obterLimiteSaque(TipoConta tipoConta) {

        return switch (tipoConta) {
            case BASICA -> new BigDecimal("3000.00");
            case GOLD -> new BigDecimal("10000.00");
        };
    }

    private BigDecimal obterLimiteDiario(TipoConta tipoConta) {

        return switch (tipoConta) {
            case BASICA -> new BigDecimal("5000.00");
            case GOLD -> new BigDecimal("15000.00");
        };
    }

    private void validarLimiteSaque(Conta conta, BigDecimal valorSaque) {

        BigDecimal limite = obterLimiteSaque(conta.getTipoConta());

        log.debug(
                "event=validacao_limite_saque conta_id={} tipo_conta={} valor_saque={} limite={}",
                conta.getIdConta(),
                conta.getTipoConta(),
                valorSaque,
                limite
        );

        //se o valorSaque for maior que 0, então estou tentando sacar um valor maior do que o meu limite por saque
        if (valorSaque.compareTo(limite) > 0) {

            log.warn(
                    "event=limite_saque_excedido conta_id={} tipo_conta={} valor_saque={} limite={}",
                    conta.getIdConta(),
                    conta.getTipoConta(),
                    valorSaque,
                    limite
            );

            throw new ResponseStatusException(
                    HttpStatus.UNPROCESSABLE_ENTITY,
                    "Valor ultrapassa o limite permitido para o tipo da sua conta: " + conta.getTipoConta()
            );
        }
    }

    private OffsetDateTime getInicioDodia() {
        return OffsetDateTime.now()
                .toLocalDate()
                .atStartOfDay()
                .atOffset(ZoneOffset.UTC);
    }

    public List<Saque> obterSaquesHoje(Conta conta) {
        return saqueRepository.findByIdContaAndDataHoraCriacaoAfter(conta.getIdConta(), getInicioDodia());
    }

    private void validarLimiteDiario(Conta conta, BigDecimal valorSaque) {
        BigDecimal totalHoje = obterSaquesHoje(conta).stream()
                .map(Saque::getValor)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);

        BigDecimal limiteDiario = obterLimiteDiario(conta.getTipoConta());

        log.debug(
                "event=validacao_limite_diario conta_id={} tipo_conta={} total_hoje={} valor_saque={} limite_diario={}",
                conta.getIdConta(),
                conta.getTipoConta(),
                totalHoje,
                valorSaque,
                limiteDiario
        );

        if (totalHoje.add(valorSaque).compareTo(limiteDiario) > 0) {

            log.warn(
                    "event=limite_diario_excedido conta_id={} tipo_conta={} total_hoje={} valor_saque={} limite_diario={}",
                    conta.getIdConta(),
                    conta.getTipoConta(),
                    totalHoje,
                    valorSaque,
                    limiteDiario
            );

            throw new ResponseStatusException(
                    HttpStatus.UNPROCESSABLE_ENTITY,
                    "Saque ultrapassa o limite diário da conta: " + limiteDiario
            );
        }
    }

    private Saque criarESalvarSaque(Conta conta, BigDecimal valorSaque) {
        OffsetDateTime dataHoraCriacao = OffsetDateTime.now();

        log.info(
                "event=criando_saque conta_id={} valor={} timestamp={}",
                conta.getIdConta(),
                valorSaque,
                dataHoraCriacao
        );

        Saque saque = new Saque();
        saque.setIdConta(conta.getIdConta());
        saque.setValor(valorSaque);
        saque.setDataHoraCriacao(dataHoraCriacao);

        Saque salvo = saqueRepository.save(saque);

        log.info(
                "event=saque_persistido saque_id={} conta_id={} valor={}",
                salvo.getId(),
                salvo.getIdConta(),
                salvo.getValor()
        );

        return salvo;
    }

    private SaqueResponse montarSaqueResponse(Conta conta, Saque saque, String canal) {
        BigDecimal saldoAntes = conta.getSaldo().setScale(2, RoundingMode.HALF_UP);

        log.info(
                "event=debito_conta_inicio conta_id={} saldo_antes={} valor_saque={}",
                conta.getIdConta(),
                saldoAntes,
                saque.getValor()
        );

        BigDecimal saldoDepois = saldoAntes.subtract(saque.getValor());
        conta.setSaldo(saldoDepois);


        try {
            contaRepository.save(conta);

            log.info(
                    "event=saldo_atualizado conta_id={} saldo_depois={}",
                    conta.getIdConta(),
                    saldoDepois
            );

        } catch (OptimisticLockingFailureException e) {

            log.warn(
                    "event=concorrencia_detectada conta_id={} valor={}",
                    conta.getIdConta(),
                    saque.getValor()
            );

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Operação concorrente detectada. Tente novamente."
            );
        }

        return new SaqueResponse(
                saque.getId().toHexString(),
                conta.getIdConta(),
                saque.getValor(),
                canal,
                StatusSaque.APROVADA,
                saldoAntes,
                saldoDepois,
                saque.getDataHoraCriacao()
        );
    }

    public void validarLimiteCanal(Canal canal, BigDecimal valorSaque) {
        BigDecimal limiteATM = new BigDecimal("1000");

        log.debug(
                "event=validacao_limite_canal canal={} valor_saque={} limite_atm={}",
                canal,
                valorSaque,
                limiteATM
        );

        if (canal == Canal.ATM && valorSaque.compareTo(limiteATM) > 0) {

            log.warn(
                    "event=limite_canal_excedido canal={} valor_saque={} limite_atm={}",
                    canal,
                    valorSaque,
                    limiteATM
            );

            throw new ResponseStatusException(
                    HttpStatus.UNPROCESSABLE_ENTITY,
                    "Limite por saque em ATM é de R$1.000,00"
            );
        }
    }

    private IdSaqueResponse montarIdSaqueResponse(Saque saque) {

        log.debug(
                "event=montando_resposta_saque saque_id={} conta_id={} valor={}",
                saque.getId(),
                saque.getIdConta(),
                saque.getValor()
        );

        return new IdSaqueResponse(
                saque.getId().toHexString(),
                saque.getIdConta(),
                saque.getValor(),
                saque.getDataHoraCriacao()
        );
    }

    public IdSaqueResponse consultarSaqueId(String idSolicitacaoSaque) {

        log.debug(
                "event=consulta_saque_inicio saque_id={}",
                idSolicitacaoSaque
        );

        ObjectId id = new ObjectId(idSolicitacaoSaque);

        Saque saque = saqueRepository.findById(id)
                .orElseThrow(() -> {

                    log.warn(
                            "event=saque_nao_encontrado saque_id={}",
                            idSolicitacaoSaque
                    );

                    return new ResponseStatusException(
                            HttpStatus.NOT_FOUND,
                            "Saque não encontrado"
                    );
                });

        return montarIdSaqueResponse(saque);
    }

    @Transactional
    public void sacarComErroTest(String idConta, BigDecimal valor) {
        log.info("Iniciando saque para conta {}", idConta);

        Conta conta = contaRepository.findById(idConta)
                .orElseThrow(() -> new RuntimeException("Conta não encontrada"));

        log.info("Saldo antes do saque: {}", conta.getSaldo());

        // debita saldo
        conta.setSaldo(conta.getSaldo().subtract(valor));
        contaRepository.save(conta);

        log.info("Saldo após débito: {}", conta.getSaldo());

        log.warn("Simulando erro para testar rollback...");

        // ERRO proposital para testar rollback
        if (true) {
            throw new RuntimeException("Erro proposital para testar atomicidade");
        }

        // nunca será executado
        Saque saque = new Saque();
        saque.setIdConta(idConta);
        saque.setValor(valor);

        saqueRepository.save(saque);
    }
}
