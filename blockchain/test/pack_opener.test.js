// Importa os artefatos dos contratos
const CardNFT = artifacts.require("CardNFT");
const PackOpener = artifacts.require("PackOpener");

// `contract` é uma suíte de testes do Truffle
contract("PackOpener", (accounts) => {
  // Variáveis para guardar as instâncias dos contratos
  let nftInstance;
  let openerInstance;

  // Contas
  const deployer = accounts[0];
  const player = accounts[1];

  // ----- CORREÇÃO AQUI -----
  // Trocamos 'before' por 'beforeEach'
  // 'beforeEach' roda ANTES DE CADA 'it(...)'
  beforeEach(async () => {
    // 1. Implanta um NOVO CardNFT para este teste
    nftInstance = await CardNFT.new({ from: deployer });

    // 2. Implanta um NOVO PackOpener, linkando ao novo NFT
    openerInstance = await PackOpener.new(nftInstance.address, {
      from: deployer,
    });

    // 3. Autoriza o novo PackOpener no novo CardNFT
    await nftInstance.setPackOpener(openerInstance.address, { from: deployer });
  });
  // ----- FIM DA CORREÇÃO -----

  it("deve abrir um pacote e mintar 5 cartas para o jogador", async () => {
    // Este teste agora roda com um packCounter zerado
    let cardsBefore = await nftInstance.getPlayerCards(player);
    assert.equal(
      cardsBefore.length,
      0,
      "O jogador não deve ter cartas no início"
    );

    console.log("Jogador", player, "está abrindo um pacote...");
    await openerInstance.openPack({ from: player });

    let cardsAfter = await nftInstance.getPlayerCards(player);
    assert.equal(
      cardsAfter.length,
      5,
      "O jogador deve ter 5 cartas após abrir o pacote"
    );

    const counter = await openerInstance.packCounter();
    assert.equal(counter.toString(), "1", "O contador de pacotes deve ser 1");

    const firstCardId = cardsAfter[0].toString();
    const cardData = await nftInstance.cards(firstCardId);

    console.log("Carta mintada (ID:", firstCardId, "):", cardData.name);
    assert.isNotEmpty(cardData.name, "A carta mintada deve ter um nome");
  });

  it("deve abrir 10 pacotes corretamente", async () => {
    // Este teste AGORA TAMBÉM roda com um packCounter zerado,
    // graças ao 'beforeEach'
    const packsToOpen = 10;

    console.log(`Simulando abertura de ${packsToOpen} pacotes...`);

    for (let i = 0; i < packsToOpen; i++) {
      await openerInstance.openPack({ from: player });
    }

    // A verificação agora vai funcionar
    const counter = await openerInstance.packCounter();
    assert.equal(
      counter.toString(),
      packsToOpen.toString(), // Espera 10
      "Contador deve ser igual a 10" // Vai receber 10
    );

    let cardsAfter = await nftInstance.getPlayerCards(player);
    assert.equal(
      cardsAfter.length,
      5 * packsToOpen, // 50 cartas
      "O jogador deve ter 50 cartas (10 pacotes * 5 cartas)"
    );
  });
});
