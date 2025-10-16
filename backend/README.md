# 🌀 Avatar Online - Cluster de Teste

Este projeto implementa um sistema distribuído com suporte a múltiplos nós em um cluster, utilizando **Docker Compose** para simulação local.

---

## 🚀 Como testar o cluster completo

Para iniciar **todas as instâncias** do cluster (múltiplos nós e bancos de dados), execute:

```bash
docker-compose -f cluster-test.yml up --build
```

No windows.

```bash
docker-compose -f docker-compose-windows up --build
```

Esse comando irá:

* Criar e subir todos os containers definidos no arquivo `cluster-test.yml`;
* Construir novamente as imagens, garantindo que você está rodando a versão mais recente do código;
* Iniciar automaticamente os nós do cluster que se comunicam entre si.

Após o build, os logs de cada nó serão exibidos no terminal.

---

## 🧩 Como testar um nó individual

Para subir **apenas um nó** (sem o cluster completo), utilize:

```bash
docker-compose up --build
```

Isso executará apenas a aplicação e o banco de dados local definidos no `docker-compose.yml`.

---

## 👤 Como testar a criação de usuários

Após subir o container (de um nó ou do cluster), você pode criar um novo usuário utilizando o comando `curl`:

```bash
curl -X POST http://localhost:8081/api/users \
     -H "Content-Type: application/json" \
     -d '{
           "nickname": "L",
           "name": "Elinaldo",
           "email": "elinaldo@example.com",
           "password": "123456"
         }'
```

📌 **Observações:**

* Altere o `localhost:8081` para a porta do nó que deseja testar (ex: `8082`, `8083`, etc.);
* O endpoint `/api/users` transfere a responsabilidade para o líder cria um novo usuário;
* O sistema replica automaticamente as informações entre os nós do cluster (se configurado corretamente).

---