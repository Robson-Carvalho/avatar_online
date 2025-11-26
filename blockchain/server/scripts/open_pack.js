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
    element: ELEMENT_MAP[Number(dados.element)],
    phase: PHASE_MAP[Number(dados.phase)],
    rarity: RARITY_MAP[Number(dados.rarity)],
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

    const playerAddress = process.argv[4];

    if (!playerAddress) {
      console.log(
        JSON.stringify({ error: "Endereço do jogador é obrigatório" })
      );
      return callback();
    }

    if (!web3.utils.isAddress(playerAddress)) {
      console.log(JSON.stringify({ error: "Endereço inválido" }));
      return callback();
    }

    const accounts = await web3.eth.getAccounts();
    const fromAccount = accounts[0];

    const cartasAntes = (await nft.getPlayerCards(playerAddress)).map((id) =>
      id.toString()
    );

    const tx = await opener.openPackForPlayer(playerAddress, {
      from: fromAccount,
    });

    await new Promise((resolve) => setTimeout(resolve, 2000));

    const cartasDepois = (await nft.getPlayerCards(playerAddress)).map((id) =>
      id.toString()
    );

   const novasCartasIds = [...new Set(
  cartasDepois.filter(id => !cartasAntes.includes(id))
)];


    const cartasDoPack = [];
    for (const id of novasCartasIds) {
      const dados = await nft.getCard(id);
      cartasDoPack.push(formatarCartaParaJSON(id, dados));
    }

    const result = {
      player: playerAddress,
      transaction: tx.tx,
      newCardsFromPack: cartasDoPack.length,
      cartasDoPack,
    };

    console.info("###JSON_START###" + JSON.stringify(result, null, 2));

    callback();
  } catch (error) {
    console.error("Erro:", error);
    callback(error);
  }
};
