const MatchResult = artifacts.require("MatchResult");

contract("MatchResult", (accounts) => {
  const player1 = "Guilherme";
  const player2 = "Joao";
  const winner = "Guilherme";

  let instance;

  before(async () => {
    instance = await MatchResult.deployed();
  });

  it("deve iniciar com totalMatches = 0", async () => {
    const total = await instance.totalMatches();
    assert.equal(total.toNumber(), 0, "totalMatches deveria começar em 0");
  });

  it("deve registrar uma nova partida", async () => {
    const tx = await instance.recordMatch(player1, player2, winner, {
      from: accounts[0],
    });

    assert.equal(
      tx.logs[0].event,
      "MatchRecorded",
      "Evento MatchRecorded deveria ser emitido"
    );

    assert.equal(tx.logs[0].args.matchId.toNumber(), 0, "ID deveria ser 0");
    assert.equal(tx.logs[0].args.player1, player1, "player1 incorreto");
    assert.equal(tx.logs[0].args.player2, player2, "player2 incorreto");
    assert.equal(tx.logs[0].args.winner, winner, "winner incorreto");
  });

  it("deve aumentar totalMatches para 1", async () => {
    const total = await instance.totalMatches();
    assert.equal(total.toNumber(), 1, "totalMatches deveria ser 1");
  });

  it("deve recuperar corretamente os dados da partida 0", async () => {
    const match0 = await instance.matches(0);

    assert.equal(match0.player1, player1, "player1 incorreto");
    assert.equal(match0.player2, player2, "player2 incorreto");
    assert.equal(match0.winner, winner, "winner incorreto");
  });
});
