// Importa os contratos
const CardNFT = artifacts.require("CardNFT");
const PackOpener = artifacts.require("PackOpener");

module.exports = async function (callback) {
  try {
    console.log("Iniciando simulação de novo jogador...");

    // 1. Pegar contratos e a conta principal do Ganache (que tem ETH)
    const nft = await CardNFT.deployed();
    const opener = await PackOpener.deployed();
    const accounts = await web3.eth.getAccounts();
    const contaDoDono = accounts[0]; // A conta que vai financiar

    // 2. CRIAR a nova conta
    console.log("Criando uma nova carteira (conta) do zero...");
    const novoJogador = web3.eth.accounts.create();
    const novoEndereco = novoJogador.address;
    const novaChavePrivada = novoJogador.privateKey;

    console.log(`Endereço do novo jogador: ${novoEndereco}`);

    // 3. FINANCIAR a nova conta
    console.log(
      `Enviando 1 ETH (falso) de ${contaDoDono.substring(
        0,
        10
      )}... para ${novoEndereco.substring(0, 10)}...`
    );

    await web3.eth.sendTransaction({
      from: contaDoDono,
      to: novoEndereco,
      value: web3.utils.toWei("5", "ether"), // Enviando 1 ETH
    });

    const balanco = await web3.eth.getBalance(novoEndereco);
    console.log(
      `Financiamento concluído! Novo saldo: ${web3.utils.fromWei(
        balanco,
        "ether"
      )} ETH`
    );

    // 4. USAR a nova conta (MÉTODO DE ASSINATURA MANUAL)
    console.log(
      "Novo jogador está abrindo um pacote (assinando manualmente)..."
    );

    // 4a. Preparar os dados da transação (o "payload")
    // Isso diz à blockchain qual função chamar: "openPack()"
    const dataPayload = opener.contract.methods.openPack().encodeABI();

    // 4b. Estimar o gás
    const gasEstimado = await opener.openPack.estimateGas({
      from: novoEndereco,
    });
    const gasPrice = await web3.eth.getGasPrice();

    // 4c. Criar o objeto da transação
    const txObject = {
      from: novoEndereco,
      to: opener.address, // O endereço do contrato PackOpener
      nonce: await web3.eth.getTransactionCount(novoEndereco, "latest"), // Nonce da conta
      gas: gasEstimado,
      gasPrice: gasPrice,
      data: dataPayload, // A função a ser chamada
    };

    // 5. Assinar a transação LOCALMENTE com a chave privada
    console.log("Assinando a transação localmente...");
    const signedTx = await web3.eth.accounts.signTransaction(
      txObject,
      novaChavePrivada
    );

    // 6. Enviar a transação JÁ ASSINADA
    console.log("Enviando transação assinada para a rede...");
    const receipt = await web3.eth.sendSignedTransaction(
      signedTx.rawTransaction
    );

    console.log(
      `Pacote aberto! (Hash: ${receipt.transactionHash.substring(0, 10)}...)`
    );

    // 7. Verificar o resultado
    const cardIds = await nft.getPlayerCards(novoEndereco);

    console.log("\n--- SUCESSO! ---");
    console.log(
      `O novo jogador ${novoEndereco} abriu um pacote e agora possui ${cardIds.length} cartas.`
    );
    console.log("----------------\n");

    callback(); // Sucesso
  } catch (error) {
    console.error("Ocorreu um erro na simulação:", error);
    callback(error); // Erro
  }
};
