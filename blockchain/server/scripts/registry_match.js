const MatchResultArtifact = artifacts.require("MatchResult");

module.exports = async function (callback) {
  try {
    console.log("---- Endpoint Fake: Registrar Partida ----");

    const args = process.argv.slice(4);

    if (args.length < 3) {
      console.log("\nUso correto:");
      console.log("truffle exec scripts/registry_match.js <player1> <player2> <winner>\n");
      return callback();
    }

    const [player1, player2, winner] = args;

    console.log("\nDados recebidos da 'API':");
    console.log("Player1:", player1);
    console.log("Player2:", player2);
    console.log("Winner :", winner, "\n");

    const MatchResult = await MatchResultArtifact.deployed();

    const accounts = await web3.eth.getAccounts();
    const remetente = accounts[0];

    console.log("Registrando partida na blockchain...");

    const tx = await MatchResult.recordMatch(player1, player2, winner, {
      from: remetente,
      gas: 300000,
    });

    console.log("Transação enviada:", tx.tx);

    const total = await MatchResult.totalMatches();
    const lastId = total.toNumber() - 1;

    const matchStored = await MatchResult.matches(lastId);

    console.log("\n--- Partida salva ---");
    console.log("ID:", lastId);
    console.log("Player 1:", matchStored.player1);
    console.log("Player 2:", matchStored.player2);
    console.log("Winner :", matchStored.winner);
    console.log("----------------------\n");

    callback();
  } catch (error) {
    console.error("❌ Erro ao registrar partida:", error);
    callback(error);
  }
};
