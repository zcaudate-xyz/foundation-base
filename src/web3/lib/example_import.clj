(ns web3.lib.example-erc20-source
  (:require [std.lang :as l]))

(l/script :solidity
  {:require [[rt.solidity :as s]]
   :static  {:contract ["ERC20Basic"]}})

