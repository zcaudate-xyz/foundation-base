(ns web3.lib.example-erc20-source
  (:require [hara.lang :as l]))

(l/script :solidity
  {:require [[hara.runtime.solidity :as s]]
   :static  {:contract ["ERC20Basic"]}})

