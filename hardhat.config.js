// Disable the Hardhat compilation watcher. The tests start/stop the node
// repeatedly and the watcher exhausts file descriptors while polling the
// artifacts/build-info directory.
const watch = require('hardhat/builtin-tasks/utils/watch');
watch.watchCompilerOutput = async () => ({ close: async () => {} });

module.exports = {
  solidity: '0.8.34',
  networks: {
    hardhat: {
      chainId: 1337,
      accounts: {
        mnemonic: 'taxi dash nation raw first art ticket more useful mosquito include true',
        path: "m/44'/60'/0'/0",
        count: 10,
      },
    },
  },
};
