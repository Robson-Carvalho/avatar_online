const express = require("express");
const cors = require("cors");
const { exec } = require("child_process");

const app = express();

app.use(
  cors({
    origin: "*",
    methods: ["GET", "POST", "PUT", "DELETE"],
    credentials: true,
  })
);
app.use(express.json());
app.use(express.urlencoded({ extended: true }));

const PORT = 3000;

function run_process(cmd, res) {
  exec(
    cmd,
    {
      cwd: __dirname,
      maxBuffer: 1024 * 1024,
    },
    (error, stdout, stderr) => {
      if (error) {
        return res.status(500).json({
          status: "error",
          message: error.message,
          stdout,
          stderr,
        });
      }

      console.log(stdout);

      let data = null;

      const regex = /###JSON_START###([\s\S]*)$/;
      const match = stdout.match(regex);

      if (match) {
        try {
          data = JSON.parse(match[1]);
        } catch (e) {
          console.log("JSON inválido! " + e);
        }
      }

      return res.json({
        status: "success",
        command: cmd,
        data,
      });
    }
  );
}

app.get("/", (req, res) => {
  res.json({
    message:
      "🚀 Servidor Node.js ativo! Integração com Blockchain funcionando perfeitamente.",
    status: "online",
    description:
      "API para gerenciamento de contratos inteligentes, contas e interações Web3.",
    timestamp: new Date().toISOString(),
  });
});

app.get("/compile", (req, res) => {
  run_process("truffle compile", res);
});

app.get("/migrate", (req, res) => {
  run_process("truffle migrate --reset", res);
});

app.get("/test", (req, res) => {
  run_process("truffle test", res);
});

app.get("/get_cards_account_ganache", (req, res) => {
  run_process("truffle exec scripts/get_cards_account_ganache.js", res);
});

app.post("/get_cards", (req, res) => {
  const { address } = req.body;

  if (!address) {
    return res.status(400).json({ error: "endereço é obrigatório" });
  }

  run_process(`truffle exec scripts/get_cards.js ${address}`, res);
});

app.get("/create_account", (req, res) => {
  run_process("truffle exec scripts/create_account.js", res);
});

app.post("/open_pack", (req, res) => {
  const { address } = req.body;

  if (!address) {
    return res.status(400).json({ error: "endereço é obrigatório" });
  }

  run_process(`truffle exec scripts/open_pack.js ${address}`, res);
});

app.post("/registry_match", (req, res) => {
  const { player1, player2, winner } = req.body;

  if (!player1 || !player2 || !winner) {
    return res
      .status(400)
      .json({ error: "Corpo com informações insuficientes ou corrompido" });
  }

  run_process(
    `truffle exec scripts/registry_match.js ${player1} ${player2} ${winner}`,
    res
  );
});

app.post("/swap_cards", (req, res) => {
  const { player1, cardId1, player2, cardId2 } = req.body;

  if (!player1 || !cardId1 || !player2 || !cardId2) {
    return res.status(400).json({
      error:
        "Todos os parâmetros são obrigatórios: player1, cardId1, player2, cardId2",
    });
  }

  run_process(
    `truffle exec scripts/swap_cards.js ${player1} ${cardId1} ${player2} ${cardId2}`,
    res
  );
});

app.get("/history", (req, res) => {
  run_process("truffle exec scripts/getHistory.js", res);
});

app.listen(PORT, () => {
  console.log(`NodeJS API running on http://localhost:${PORT}`);
});
