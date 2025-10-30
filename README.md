# 🧪 Testes de Software — Validação do Sistema Distribuído

## 1. Objetivo dos Testes

Os testes de software têm como objetivo **avaliar a robustez e a justiça da solução distribuída**, garantindo que:
- O **algoritmo de consenso Raft** mantenha a consistência entre os nós durante operações concorrentes;
- O sistema se comporte corretamente em **situações de falha e redirecionamento de requisições**;
- O desempenho sob carga seja **aceitável** (tempo de resposta, taxa de sucesso, throughput).

Esses testes também foram projetados para **reproduzir cenários reais de uso**, simulando múltiplos usuários executando operações simultâneas como criação de contas, abertura de pacotes e atualização de baralhos.

---

## 2. Ambiente de Teste

Os testes foram executados em um ambiente **Dockerizado**, composto por três nós Raft independentes, cada um com sua própria instância de banco de dados PostgreSQL.

### Estrutura dos Contêineres

| Componente | Descrição |
|-------------|------------|
| `avatar-node1` | Nó líder (coordena a replicação dos logs) |
| `avatar-node2` | Nó seguidor (replica e valida as entradas) |
| `avatar-node3` | Nó seguidor (replica e valida as entradas) |
| `postgres-nodeX` | Banco de dados dedicado a cada nó |

Cada contêiner é definido em seu respectivo `docker-compose.yml`, com **endereços fixos** configurados para permitir a comunicação entre os nós.

---

## 3. Execução dos Testes

Os testes foram implementados em **Python**, utilizando múltiplas threads para simular usuários concorrentes interagindo com o sistema.

### Principais Scripts

| Script | Descrição |
|---------|------------|
| `stress_createUser_test.py` | Simula múltiplos usuários realizando cadastro simultâneo. Avalia o redirecionamento automático de requisições para o líder e a consistência dos dados cadastrados. |
| `stress_openPack_test.py` | Realiza a abertura de pacotes de cartas simultaneamente, validando se o estado do jogo é corretamente replicado em todos os nós. |
| `stress_deckUpdate_test.py` | Testa a atualização de baralhos dos jogadores, verificando se as mudanças são aplicadas de forma consistente após o commit no líder. |
| `stress_tradeCard_test.py` | Teste a troca de cartas entre jogadores, verificando se as mudanças foram aplicadas corretamente entre os nós |

---

## 4. Execução dos Testes Manualmente

Para rodar os testes individualmente:

```bash
# Subir o cluster distribuído
docker-compose -f docker-compose-develop.yml up --build

# Executar o teste de criação de usuários
python3 stress_createUser_test.py

# Executar o teste de abertura de pacotes
python3 stress_openPack_test.py

# Executar o teste de atualização de baralhos
python3 stress_deckUpdate_test.py

# Executar o teste de troca de cartas
python3 stress_tradeCard_test.py
```

Os resultados dos testes podem ser vistos em arquivos .json da pasta /backend/stress_test para melhor analise e visualização. 
## 5. Resultados Esperados

Durante a execução dos testes, o sistema deve:
- Detectar automaticamente o nó líder e redirecionar as requisições para ele;
- Garantir que apenas após o commit majoritário as operações sejam consideradas concluídas;
- Manter a consistência dos dados entre todos os nós mesmo após falhas simuladas.

Os logs esperados incluem mensagens como:
```bash
🚫 Este nó não é o líder. Redirecionando para o líder...
🔄 Redirecionando requisição para o líder: http://avatar-node1:8080/api/users
💕 Heartbeat enviado para propagação de log.
🎉 SUCESSO! Log persistido em 3 nós (Maioria alcançada: 2).
```

## 6. Cenários de Falha

Além dos testes de carga, também foram simuladas situações de falha:

- Interrupção do líder durante commits ativos;
- Latência de rede e perda de pacotes entre os nós;
- Condições de concorrência extrema, com múltiplas requisições simultâneas em nós diferentes.

Nesses casos, o sistema deve:

- Eleger automaticamente um novo líder (Raft reconfigura o cluster);
- Retornar respostas HTTP adequadas (ex: 503 Service Unavailable durante eleição);
- Garantir que nenhum dado seja perdido após a reconexão.

## 7. Métricas Avaliadas

| Métrica | Descrição |
|---------|------------|
| `Tempo médio de resposta (ms)` | Mede a latência entre requisição e resposta final. |
| `Throughput (req/s)` | Número de requisições processadas por segundo. |
| `Taxa de sucesso (%)` | Percentual de requisições concluídas sem erro. |
---
