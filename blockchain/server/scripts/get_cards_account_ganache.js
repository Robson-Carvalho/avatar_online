const CardNFT = artifacts.require("CardNFT");
const PackOpener = artifacts.require("PackOpener");

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
    const nft = await CardNFT.deployed();
    const opener = await PackOpener.deployed();

    const accounts = await web3.eth.getAccounts();
    const player = accounts[0];

    console.log(`Abrindo pack para ${player}`);

    await opener.openPack({ from: player });

    const cardIds = await nft.getPlayerCards(player);

    const cartas = [];

    for (const id of cardIds) {
      const dados = await nft.getCard(id);
      cartas.push(formatarCartaParaJSON(id, dados));
    }

    console.info("###JSON_START###" + JSON.stringify(cartas, null, 2));
    callback();
  } catch (error) {
    console.error("Erro:", error);
    callback(error);
  }
};
