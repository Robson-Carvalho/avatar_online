module.exports = {
  networks: {
    development: {
      host: "172.17.0.1",
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
