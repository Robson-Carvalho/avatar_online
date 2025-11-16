module.exports = async function (callback) {
  try {
    console.log("Iniciando script de criação de conta...");

    // 1. Pegar a conta principal do Ganache (que tem ETH)
    const accounts = await web3.eth.getAccounts();
    const contaDoDono = accounts[0]; // A conta que vai financiar

    if (!contaDoDono) {
      throw new Error(
        "Não foi possível encontrar a conta principal (accounts[0])"
      );
    }

    // 2. CRIAR a nova conta
    console.log("Criando uma nova carteira (conta) do zero...");
    const novoJogador = web3.eth.accounts.create();
    const novoEndereco = novoJogador.address;
    const novaChavePrivada = novoJogador.privateKey;

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
      value: web3.utils.toWei("1", "ether"), // Enviando 1 ETH
    });

    const balanco = await web3.eth.getBalance(novoEndereco);

    // 4. Imprimir os detalhes da nova conta
    console.log("\n--- ✅ SUCESSO! ---");
    console.log("Nova conta criada e financiada.");
    console.log(`Endereço (Address): ${novoEndereco}`);
    console.log(`Chave Privada (PrivateKey): ${novaChavePrivada}`);
    console.log(`Saldo (Balance): ${web3.utils.fromWei(balanco, "ether")} ETH`);
    console.log("------------------\n");
    console.log(
      "🚨 Guarde esta Chave Privada em segurança! Você pode importá-la no MetaMask."
    );

    callback(); // Sucesso
  } catch (error) {
    console.error("Ocorreu um erro ao criar a conta:", error);
    callback(error); // Erro
  }
};
