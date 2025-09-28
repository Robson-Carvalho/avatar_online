# Rodar para teste

Abra quantos terminais quiser, depois rode o seguinte comando em cada terminal

```bash
mvn spring-boot:run -Dspring-boot.run.arguments="--server.port=808x --node.base-url=http://localhost:808x --node.peer=server0x"
```

Substitua o x pelo servidor correspondente (1, 2, 3, ...), assim vc verá a comunicação estabelecida e as mensagens sendo enviadas