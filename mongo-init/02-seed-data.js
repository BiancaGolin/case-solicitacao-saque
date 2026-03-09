print("Aguardando ReplicaSet ficar PRIMARY...");

while (true) {

    try {

        const status = rs.status();

        if (status.myState === 1) {
            print("ReplicaSet PRIMARY detectado!");
            break;
        }

    } catch (e) {

        print("ReplicaSet ainda não iniciado. Tentando iniciar...");

        try {
            rs.initiate({
                _id: "rs0",
                members: [{ _id: 0, host: "mongo:27017" }]
            });
        } catch (initError) {
            print("ReplicaSet já iniciado ou aguardando eleição...");
        }
    }

    sleep(1000);
}

db = db.getSiblingDB('saque-db');  // seleciona/cria o banco

db.accounts.insertMany([
    { _id: "c101", status: "ATIVA", saldo: NumberDecimal("10000.00"), tipoConta: "GOLD", version: 0 },
    { _id: "c102", status: "ATIVA", saldo: NumberDecimal("3000.00"), tipoConta: "BASICA", version: 0 },
    { _id: "c103", status: "ATIVA", saldo: NumberDecimal("7000.00"), tipoConta: "GOLD", version: 0 },
    { _id: "c104", status: "ENCERRADA", saldo: NumberDecimal("1500.00"), tipoConta: "BASICA", version: 0 },
    { _id: "c105", status: "ATIVA", saldo: NumberDecimal("5000.00"), tipoConta: "GOLD", version: 0 },
    { _id: "c106", status: "ATIVA", saldo: NumberDecimal("10000.00"), tipoConta: "GOLD", version: 0 },
    { _id: "c107", status: "ENCERRADA", saldo: NumberDecimal("30600.00"), tipoConta: "BASICA", version: 0 },
    { _id: "c108", status: "ATIVA", saldo: NumberDecimal("70000.00"), tipoConta: "GOLD", version: 0 },
    { _id: "c109", status: "ENCERRADA", saldo: NumberDecimal("1500.00"), tipoConta: "BASICA", version: 0 },
    { _id: "c110", status: "ATIVA", saldo: NumberDecimal("5020.00"), tipoConta: "GOLD", version: 0 }
]);

const now = new Date();

db.saques.insertMany([
    { idConta: "c101", valor: NumberDecimal("500.00"), dataHoraCriacao: now },
    { idConta: "c101", valor: NumberDecimal("200.00"), dataHoraCriacao: now },
    { idConta: "c101", valor: NumberDecimal("300.00"), dataHoraCriacao: now },

    { idConta: "c102", valor: NumberDecimal("1000.00"), dataHoraCriacao: now },
    { idConta: "c102", valor: NumberDecimal("500.00"), dataHoraCriacao: now },
    { idConta: "c102", valor: NumberDecimal("300.00"), dataHoraCriacao: now },

    { idConta: "c103", valor: NumberDecimal("1500.00"), dataHoraCriacao: now },
    { idConta: "c103", valor: NumberDecimal("700.00"), dataHoraCriacao: now },
    { idConta: "c103", valor: NumberDecimal("400.00"), dataHoraCriacao: now },

    { idConta: "c104", valor: NumberDecimal("300.00"), dataHoraCriacao: now },
    { idConta: "c104", valor: NumberDecimal("200.00"), dataHoraCriacao: now },
    { idConta: "c104", valor: NumberDecimal("100.00"), dataHoraCriacao: now },

    { idConta: "c105", valor: NumberDecimal("1000.00"), dataHoraCriacao: now },
    { idConta: "c105", valor: NumberDecimal("500.00"), dataHoraCriacao: now },
    { idConta: "c105", valor: NumberDecimal("400.00"), dataHoraCriacao: now },

    { idConta: "c106", valor: NumberDecimal("500.00"), dataHoraCriacao: now },
    { idConta: "c106", valor: NumberDecimal("200.00"), dataHoraCriacao: now },
    { idConta: "c106", valor: NumberDecimal("300.00"), dataHoraCriacao: now },

    { idConta: "c107", valor: NumberDecimal("1000.00"), dataHoraCriacao: now },
    { idConta: "c107", valor: NumberDecimal("500.00"), dataHoraCriacao: now },

    { idConta: "c108", valor: NumberDecimal("1500.00"), dataHoraCriacao: now },
    { idConta: "c108", valor: NumberDecimal("700.00"), dataHoraCriacao: now },
    { idConta: "c108", valor: NumberDecimal("400.00"), dataHoraCriacao: now },

    { idConta: "c109", valor: NumberDecimal("300.00"), dataHoraCriacao: now },
    { idConta: "c109", valor: NumberDecimal("200.00"), dataHoraCriacao: now },
    { idConta: "c109", valor: NumberDecimal("100.00"), dataHoraCriacao: now },
    { idConta: "c109", valor: NumberDecimal("100.00"), dataHoraCriacao: now },

    { idConta: "c110", valor: NumberDecimal("1000.00"), dataHoraCriacao: now },
    { idConta: "c110", valor: NumberDecimal("500.00"), dataHoraCriacao: now },
    { idConta: "c110", valor: NumberDecimal("400.00"), dataHoraCriacao: now },
    { idConta: "c110", valor: NumberDecimal("10.00"), dataHoraCriacao: now },
    { idConta: "c110", valor: NumberDecimal("5.00"), dataHoraCriacao: now },
    { idConta: "c110", valor: NumberDecimal("40.99"), dataHoraCriacao: now }
]);