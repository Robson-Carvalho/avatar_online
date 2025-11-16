// Importa os artefatos dos contratos
const CardNFT = artifacts.require("CardNFT");
const PackOpener = artifacts.require("PackOpener");

module.exports = async function (deployer) {
  // 1. Faz o deploy do CardNFT primeiro, pois o PackOpener depende dele
  await deployer.deploy(CardNFT);

  // 2. Pega a instância do CardNFT que acabou de ser implantada
  const nftInstance = await CardNFT.deployed();
  console.log("CardNFT deployado em:", nftInstance.address);

  // 3. Faz o deploy do PackOpener, passando o endereço do CardNFT no construtor
  await deployer.deploy(PackOpener, nftInstance.address);

  // 4. Pega a instância do PackOpener
  const openerInstance = await PackOpener.deployed();
  console.log("PackOpener deployado em:", openerInstance.address);

  // 5. ETAPA CRUCIAL: Autoriza o contrato PackOpener a mintar no CardNFT
  // Chama a função setPackOpener() no CardNFT, passando o endereço do PackOpener
  console.log("Autorizando PackOpener no CardNFT...");
  await nftInstance.setPackOpener(openerInstance.address);

  console.log("Deploy e configuração concluídos!");
};
