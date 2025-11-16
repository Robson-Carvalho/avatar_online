const CardNFT = artifacts.require("CardNFT");

module.exports = async function (callback) {
  try {
    const player1Address = process.argv[4];
    const cardId1 = process.argv[5];
    const player2Address = process.argv[6];
    const cardId2 = process.argv[7];

    if (!player1Address || !cardId1 || !player2Address || !cardId2) {
      console.error(
        "Uso: truffle exec scripts/swap_cards.js <player1> <cardId1> <player2> <cardId2>"
      );
      return callback(new Error("Parâmetros insuficientes"));
    }

    const nft = await CardNFT.deployed();

    // 🎯 PEGAR A PRIMEIRA CONTA DO GANACHE PARA ASSINAR
    const accounts = await web3.eth.getAccounts();
    const fromAccount = accounts[0];

    console.log("=== INICIANDO TROCA DE CARTAS ===");
    console.log(`📝 Conta assinante (servidor): ${fromAccount}`);
    console.log(`👤 Jogador 1: ${player1Address} → Carta: ${cardId1}`);
    console.log(`👤 Jogador 2: ${player2Address} → Carta: ${cardId2}`);
    console.log("=================================");

    // Verificar se as cartas existem
    try {
      await nft.getCard(cardId1);
      await nft.getCard(cardId2);
      console.log("✅ Ambas as cartas existem no contrato");
    } catch (error) {
      console.error("❌ Uma ou ambas as cartas não existem");
      return callback(error);
    }

    // Verificar propriedade das cartas
    const player1Cards = await nft.getPlayerCards(player1Address);
    const player2Cards = await nft.getPlayerCards(player2Address);

    const player1OwnsCard = player1Cards
      .map((id) => id.toString())
      .includes(cardId1);
    const player2OwnsCard = player2Cards
      .map((id) => id.toString())
      .includes(cardId2);

    if (!player1OwnsCard) {
      console.error(`❌ Jogador 1 não possui a carta ${cardId1}`);
      return callback(new Error("Jogador 1 não possui a carta especificada"));
    }

    if (!player2OwnsCard) {
      console.error(`❌ Jogador 2 não possui a carta ${cardId2}`);
      return callback(new Error("Jogador 2 não possui a carta especificada"));
    }

    console.log("✅ Ambas as cartas pertencem aos jogadores corretos");

    // 🎯 EXECUTAR TROCA USANDO A PRIMEIRA CONTA DO GANACHE
    console.log("🔄 Executando troca...");
    const tx = await nft.swapCards(
      player1Address,
      cardId1,
      player2Address,
      cardId2,
      { from: fromAccount }
    );

    console.log("✅ Troca executada com sucesso!");
    console.log(`📄 Transação hash: ${tx.tx}`);

    // Verificar resultado da troca
    const player1CardsAfter = await nft.getPlayerCards(player1Address);
    const player2CardsAfter = await nft.getPlayerCards(player2Address);

    const player1NowHasCard2 = player1CardsAfter
      .map((id) => id.toString())
      .includes(cardId2);
    const player2NowHasCard1 = player2CardsAfter
      .map((id) => id.toString())
      .includes(cardId1);

    console.log("=== VERIFICAÇÃO PÓS-TROCA ===");
    console.log(
      `👤 Jogador 1 agora possui carta ${cardId2}: ${
        player1NowHasCard2 ? "✅ SIM" : "❌ NÃO"
      }`
    );
    console.log(
      `👤 Jogador 2 agora possui carta ${cardId1}: ${
        player2NowHasCard1 ? "✅ SIM" : "❌ NÃO"
      }`
    );

    if (player1NowHasCard2 && player2NowHasCard1) {
      console.log("🎉 Troca realizada com sucesso!");

      const result = {
        transaction: tx.tx,
        signer: fromAccount,
        player1: {
          address: player1Address,
          receivedCard: cardId2,
          totalCards: player1CardsAfter.length,
        },
        player2: {
          address: player2Address,
          receivedCard: cardId1,
          totalCards: player2CardsAfter.length,
        },
      };

      console.info("###JSON_START###" + JSON.stringify(result, null, 2));
    } else {
      console.log("Erro na troca - cartas não foram transferidas corretamente");
      return callback(new Error("Falha na verificação da troca"));
    }

    callback();
  } catch (error) {
    console.error("Erro durante a troca:", error);
    callback(error);
  }
};
