const CardNFT = artifacts.require("CardNFT");
const PackOpener = artifacts.require("PackOpener");

module.exports = async function (callback) {
  try {
    console.log("Iniciando simulação de novo jogador...");

    const nft = await CardNFT.deployed();
    const opener = await PackOpener.deployed();
    const accounts = await web3.eth.getAccounts();
    const contaDoDono = accounts[0];

    // Criar conta
    const novoJogador = web3.eth.accounts.create();
    const novoEndereco = novoJogador.address;
    const novaChavePrivada = novoJogador.privateKey;

    console.log(`Endereço do novo jogador: ${novoEndereco}`);

    // Financiar
    await web3.eth.sendTransaction({
      from: contaDoDono,
      to: novoEndereco,
      value: web3.utils.toWei("5", "ether"),
    });

    const balanco = await web3.eth.getBalance(novoEndereco);

    // Abrir pack
    const dataPayload = opener.contract.methods.openPack().encodeABI();

    const gasEstimado = await opener.openPack.estimateGas({
      from: novoEndereco,
    });
    const gasPrice = await web3.eth.getGasPrice();

    const txObject = {
      from: novoEndereco,
      to: opener.address,
      nonce: await web3.eth.getTransactionCount(novoEndereco, "latest"),
      gas: gasEstimado,
      gasPrice: gasPrice,
      data: dataPayload,
    };

    const signedTx = await web3.eth.accounts.signTransaction(
      txObject,
      novaChavePrivada
    );

    await web3.eth.sendSignedTransaction(signedTx.rawTransaction);

    // Buscar cartas
    const cardIds = await nft.getPlayerCards(novoEndereco);

    const cards = [];
    for (const id of cardIds) {
      const dados = await nft.cards(id);
      cards.push({
        id: id.toString(),
        name: dados.name,
        element: dados.element.toNumber(),
        phase: dados.phase.toNumber(),
        rarity: dados.rarity.toNumber(),
        attack: dados.attack.toString(),
        life: dados.life.toString(),
        defense: dados.defense.toString(),
        description: dados.description,
      });
    }

    const jsonResult = {
      address: novoEndereco,
      private_key: novaChavePrivada,
      balance_eth: web3.utils.fromWei(balanco, "ether"),
      cards: cards,
    };

    console.log(JSON.stringify(jsonResult, null, 2));

    callback();
  } catch (error) {
    console.error("Erro:", error);
    callback(error);
  }
};
