package org.example.solicitacaosaque.model;

import lombok.Data;
import org.example.solicitacaosaque.enums.StatusConta;
import org.example.solicitacaosaque.enums.TipoConta;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;

@Data
@Document(collection = "accounts")
public class Conta {

    @Id
    private String idConta;
    private StatusConta status;
    private BigDecimal saldo;
    private TipoConta tipoConta;
    @Version
    private Long version;
}
