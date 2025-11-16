const CardNFT = artifacts.require("CardNFT");

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

function formatarCartaParaJSON(id, dados) {
  return {
    id: id.toString(),
    name: dados.name,
    element: ELEMENT_MAP[dados.element.toNumber()],
    phase: PHASE_MAP[dados.phase.toNumber()],
    rarity: RARITY_MAP[dados.rarity.toNumber()],
    attack: dados.attack.toString(),
    life: dados.life.toString(),
    defense: dados.defense.toString(),
    description: dados.description,
  };
}

module.exports = async function (callback) {
  try {
    const playerAddress = process.argv[4];
    if (!playerAddress) {
      console.log("Erro: Endereço não informado");
      return callback(new Error("Endereço não informado"));
    }

    console.log(`Buscando cartas de ${playerAddress}`);

    const nft = await CardNFT.deployed();
    const cardIds = await nft.getPlayerCards(playerAddress);

    const cartas = [];

    for (const id of cardIds) {
      const dados = await nft.cards(id);
      cartas.push(formatarCartaParaJSON(id, dados));
    }

    console.log(JSON.stringify(cartas, null, 2));

    callback();
  } catch (error) {
    console.error("Erro ao buscar cartas:", error);
    callback(error);
  }
};
