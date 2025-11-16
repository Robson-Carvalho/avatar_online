// Importa os contratos
const CardNFT = artifacts.require("CardNFT");
const PackOpener = artifacts.require("PackOpener");

// ----- ADICIONADO: Mapas de tradução dos ENUMs -----
// IMPORTANTE: A ordem DEVE ser exatamente a mesma do seu contrato Solidity
const ELEMENT_MAP = [
  "AIR",
  "BLOOD",
  "EARTH",
  "AVATAR",
  "WATER",
  "FIRE",
  "LIGHTNING",
];
const PHASE_MAP = ["COMMON", "YOUNG", "ADULT", "MASTER"];
const RARITY_MAP = ["COMMON", "EPIC", "LEGENDARY"];
// ---------------------------------------------------

/**
 * Converte os dados da struct da carta (que vem como um array)
 * para um objeto JSON limpo.
 */
function formatarCartaParaJSON(id, dadosDaCarta) {
  // --- MODIFICADO: Traduzindo os números ---
  // Os dados vêm como BigNumber (BN), então usamos .toNumber() para pegar o índice
  const elementIndex = dadosDaCarta.element.toNumber();
  const phaseIndex = dadosDaCarta.phase.toNumber();
  const rarityIndex = dadosDaCarta.rarity.toNumber();

  return {
    id: id.toString(),
    name: dadosDaCarta.name,
    // Usamos os mapas para pegar o texto correspondente
    element: ELEMENT_MAP[elementIndex],
    phase: PHASE_MAP[phaseIndex],
    rarity: RARITY_MAP[rarityIndex],
    // Stats
    attack: dadosDaCarta.attack.toString(),
    life: dadosDaCarta.life.toString(),
    defense: dadosDaCarta.defense.toString(),
    // Descrição
    description: dadosDaCarta.description,
  };
  // --- FIM DA MODIFICAÇÃO ---
}

// Esta é a função principal que o Truffle vai executar
module.exports = async function (callback) {
  try {
    console.log("Iniciando script...");

    // 1. Pegar instâncias dos contratos já implantados
    const nft = await CardNFT.deployed();
    const opener = await PackOpener.deployed();

    // 2. Pegar contas do Ganache
    const accounts = await web3.eth.getAccounts();
    const player = accounts[10]; // Usando a segunda conta como jogador

    if (!player) {
      console.log("Player não encontrado");
      return;
    }

    console.log(`Usando a conta do jogador: ${player}`);

    // 3. Abrir um pacote para esse jogador
    console.log("Abrindo um novo pacote...");
    const tx = await opener.openPack({ from: player });
    console.log(`Pacote aberto! (Hash: ${tx.tx.substring(0, 10)}...)`);

    // 4. Buscar os IDs de TODAS as cartas do jogador
    const cardIds = await nft.getPlayerCards(player);

    if (cardIds.length === 0) {
      console.log("O jogador não possui cartas.");
      callback(); // Encerra o script com sucesso
      return;
    }

    console.log(
      `O jogador agora possui ${cardIds.length} cartas. Buscando detalhes...`
    );

    // 5. Criar um array para guardar os JSONs
    const todasAsCartasJSON = [];

    // 6. Fazer um loop pelos IDs e buscar os dados de cada carta
    for (const id of cardIds) {
      const dadosDaCarta = await nft.cards(id);
      const cartaJSON = formatarCartaParaJSON(id, dadosDaCarta);
      todasAsCartasJSON.push(cartaJSON);
    }

    // 7. Imprimir o resultado final como JSON
    console.log("\n--- 🃏 CARTAS DO JOGADOR (JSON) 🃏 ---");
    console.log(JSON.stringify(todasAsCartasJSON, null, 2)); // 'null, 2' formata o JSON
    console.log("--------------------------------------\n");

    // Encerrar o script com sucesso
    callback();
  } catch (error) {
    // Encerrar o script com erro
    console.error("Ocorreu um erro no script:", error);
    callback(error);
  }
};
