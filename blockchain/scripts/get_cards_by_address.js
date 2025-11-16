// Importa o contrato
const CardNFT = artifacts.require("CardNFT");

// Mapas de tradução dos ENUMs
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

/**
 * Converte os dados da struct da carta para um objeto JSON limpo.
 */
function formatarCartaParaJSON(id, dadosDaCarta) {
  const elementIndex = dadosDaCarta.element.toNumber();
  const phaseIndex = dadosDaCarta.phase.toNumber();
  const rarityIndex = dadosDaCarta.rarity.toNumber();

  return {
    id: id.toString(),
    name: dadosDaCarta.name,
    element: ELEMENT_MAP[elementIndex],
    phase: PHASE_MAP[phaseIndex],
    rarity: RARITY_MAP[rarityIndex],
    attack: dadosDaCarta.attack.toString(),
    life: dadosDaCarta.life.toString(),
    defense: dadosDaCarta.defense.toString(),
    description: dadosDaCarta.description,
  };
}

// Função principal do script
module.exports = async function (callback) {
  try {
    // --- LÓGICA PRINCIPAL AQUI ---

    // 1. Pega o endereço da linha de comando
    // process.argv[4] é o primeiro argumento que você passa
    const playerAddress = process.argv[4];

    // 2. Validação: Verifica se um endereço foi passado
    if (!playerAddress) {
      console.error("Erro: Você esqueceu de passar o endereço do jogador!");
      console.log(
        "Exemplo de uso: truffle exec scripts/buscar_cartas.js 0xSeuEnderecoAqui"
      );
      callback(new Error("Endereço não fornecido"));
      return;
    }

    console.log(`Buscando cartas para o jogador: ${playerAddress} ...`);

    // 3. Pega a instância do contrato
    const nft = await CardNFT.deployed();

    // 4. Busca os IDs das cartas
    const cardIds = await nft.getPlayerCards(playerAddress);

    // 5. Verifica se o jogador tem cartas
    if (cardIds.length === 0) {
      console.log("Este jogador não possui nenhuma carta.");
      callback(); // Encerra com sucesso
      return;
    }

    console.log(
      `Jogador possui ${cardIds.length} cartas. Formatando para JSON...`
    );

    // 6. Formata os dados
    const todasAsCartasJSON = [];
    for (const id of cardIds) {
      const dadosDaCarta = await nft.cards(id);
      const cartaJSON = formatarCartaParaJSON(id, dadosDaCarta);
      todasAsCartasJSON.push(cartaJSON);
    }

    // 7. Imprime o resultado final
    console.log("\n--- 🃏 CARTAS ENCONTRADAS (JSON) 🃏 ---");
    console.log(JSON.stringify(todasAsCartasJSON, null, 2));
    console.log("---------------------------------------\n");

    // --- FIM DA LÓGICA ---
    callback(); // Sucesso
  } catch (error) {
    console.error("Ocorreu um erro ao buscar as cartas:", error);
    callback(error); // Erro
  }
};
