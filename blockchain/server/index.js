const express = require("express");
const cors = require("cors");
const { exec } = require("child_process");

const app = express();

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

      let data = null;
      try {
        const match = stdout.trim().match(/\{[\s\S]*\}$/);
        if (match) {
          data = JSON.parse(match[0]);
        }
      } catch (e) {
        console.log("JSON inválido (ignorando)");
      }

      return res.json({
        status: "success",
        command: cmd,
        output: stdout,
        stderr: stderr || null,
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

app.get("/get_cards", (req, res) => {
  run_process("truffle exec scripts/get_cards.js", res);
});

app.post("/get_cards", (req, res) => {
  const { address } = req.body;

  if (!address) {
    return res.status(400).json({ error: "endereço é obrigatório" });
  }

  run_process(`truffle exec scripts/get_cards_by_address.js ${address}`, res);
});

app.get("/create_account_with_cards", (req, res) => {
  run_process("truffle exec scripts/create_account.js", res);
});

app.get("/create_account", (req, res) => {
  run_process("truffle exec scripts/create_account_without_cards.js", res);
});

app.listen(PORT, () => {
  console.log(`NodeJS API running on http://localhost:${PORT}`);
});
