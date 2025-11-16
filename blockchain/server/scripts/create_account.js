module.exports = async function (callback) {
  try {
    console.log("Iniciando script de criação de conta...");

    const accounts = await web3.eth.getAccounts();
    const contaDoDono = accounts[0];

    const novaConta = web3.eth.accounts.create();
    const novoEndereco = novaConta.address;
    const novaChave = novaConta.privateKey;

    console.log(`Criando nova conta: ${novoEndereco}`);

    await web3.eth.sendTransaction({
      from: contaDoDono,
      to: novoEndereco,
      value: web3.utils.toWei("5", "ether"),
    });

    const saldo = await web3.eth.getBalance(novoEndereco);

    const result = {
      address: novoEndereco,
      private_key: novaChave,
      balance: web3.utils.fromWei(saldo, "ether"),
    };

    console.info("###JSON_START###" + JSON.stringify(result, null, 2));
    callback();
  } catch (error) {
    console.error("Erro:", error);
    callback(error);
  }
};
