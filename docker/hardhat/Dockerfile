FROM node:24-slim

WORKDIR /opt/hardhat

# Install hardhat and solc dependencies used by the foundation test suite.
# Versions are kept in sync with the root project package.json.
RUN npm install --save-dev \
    hardhat@^2.28.0 \
    ethers@^6.0.0 \
    solc@^0.8.0 \
    && npx hardhat --version

COPY hardhat.config.js ./

EXPOSE 8545

CMD ["npx", "hardhat", "node", "--hostname", "0.0.0.0", "--port", "8545"]
