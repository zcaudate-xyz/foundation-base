# Hardhat / Solc Docker Environment

This directory provides a self-contained Docker environment for running a local Hardhat node with the Solidity compiler (`solc`) available. It is intended as a reproducible fallback when the host hardhat/solc setup is unavailable or when tests need to target an isolated EVM.

## Usage

Start the node:

```bash
cd docker/hardhat
docker compose up --build -d
```

Verify it is listening:

```bash
curl -X POST -H "Content-Type: application/json" \
  --data '{"jsonrpc":"2.0","method":"eth_blockNumber","params":[],"id":1}' \
  http://127.0.0.1:8545
```

Stop it:

```bash
docker compose down
```

## Test integration

The foundation Ethereum tests (`js.lib.eth-lib-test`, `js.lib.eth-bench-test`) start and stop their own hardhat server by default. To use this container instead, ensure port `8545` is reachable before the test setup runs and adjust the test namespace setup to skip `s/rt:start-hardhat-server` / `s/rt:stop-hardhat-server`.

The container is configured with the same mnemonic and chain id (1337) used by the test suite so that the default accounts match `env-hardhat/+default-addresses+`.
