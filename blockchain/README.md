### Como executar a blockchain

```bash
  truffle compile
  truffle migrate --reset
  truffule test
  truffle exec scripts/get_cards.js
  truffle exec scripts/create_account.js
  truffle exec scripts/create_account_without_cards.js
  truffle exec scripts/get_cards_by_address.js 0x8d1c210484d04f84ac7Af4AceE6Caa02B0ff38f4 # buscar cartas de um player passando seu endereço
```

É só rodar o ganache e o truffle se conecta automáticamente.

```js
module.exports = {
  networks: {
    development: {
      host: "127.0.0.1",
      port: 7545,
      network_id: "*",
    },
  },

  compilers: {
    solc: {
      version: "0.8.20",
      settings: {
        optimizer: {
          enabled: true,
          runs: 200,
        },

        evmVersion: "berlin",
      },
    },
  },
};
```
