const MatchResultArtifact = artifacts.require("MatchResult");

module.exports = async function (callback) {
  try {
    const args = process.argv.slice(4);

    if (args.length < 3) {
      return callback();
    }

    const [player1, player2, winner] = args;
    const MatchResult = await MatchResultArtifact.deployed();

    const accounts = await web3.eth.getAccounts();
    const remetente = accounts[0];

    const tx = await MatchResult.recordMatch(player1, player2, winner, {
      from: remetente,
      gas: 300000,
    });

    const total = await MatchResult.totalMatches();
    const lastId = total.toNumber() - 1;

    const matchStored = await MatchResult.matches(lastId);

    const result = {
      player1: matchStored.player1,
      player2: matchStored.player2,
      winner: matchStored.winner,
      transaction: tx.tx,
    };

    console.info("###JSON_START###" + JSON.stringify(result, null, 2));
    callback();
  } catch (error) {
    console.error("Erro ao registrar partida:", error);
    callback(error);
  }
};
