(ns js.lib.eth-lib
  (:require [std.lang :as l]
            [std.lib :as h]
            [xt.lang.base-notify :as notify])
  (:refer-clojure :exclude [compile]))

(l/script :js
  {:require [[xt.lang.base-lib :as k]
             [js.core :as j]]
   :import [["ethers" :as [* ethers]]
            ["ethers" :as [* ethers]]]})

(def$.js ^{:arglists '([message, signature])}
  verifyMessage
  ethers.utils.verifyMessage)

(def$.js ^{:arglists '([s, units])}
  parseUnits
  ethers.utils.parseUnits)

(def$.js ^{:arglists '([s, units])}
  formatUnits
  ethers.utils.formatUnits)

(def$.js ^{:arglists '([s, units])}
  keccak256
  ethers.utils.keccak256)

(def$.js ^{:arglists '([s, units])}
  ripemd160
  ethers.utils.ripemd160)

(def$.js ^{:arglists '([s, units])}
  mnemonicToSeed
  ethers.utils.mnemonicToSeed)

(def$.js ^{:arglists '([value])}
  to-bignum
  ethers.BigNumber.from)

(h/template-entries [l/tmpl-macro {:base "Provider"
                                   :inst "p"
                                   :tag "js"}]
  [;; Accounts
   [getBalance   [address]     {:optional [blockTag]}]
   [getCode      [address]     {:optional [blockTag]}]
   [getStorageAt [address pos] {:optional [blockTag]}]
   [getTransactionCount  [address]     {:optional [blockTag]}]

   ;; Blocks
   [getBlock     [blknum]]
   [getBlockWithTransactions   [blknum]]

   ;; ENS
   [getAvatar    [name]]
   [getResolver  [name]]
   [lookupAddress  [name]]
   [resolveName  [name]]

   ;; Logs
   [getLogs      [filt]]

   ;; Network
   [getNetwork   []]
   [getBlockNumber   []]
   [getGasPrice   []]
   [getFeeData   []]

   ;; Transaction
   [call         [tx] {:optional [blockTag]}]
   [estimateGas  [tx]]
   [getTransaction [hash]]
   [getTransactionReceipt [hash]]
   [sendTransaction [tx]]
   [waitForTransaction [hash] {:optional [confirm timeout]}]

   ;; Events
   [on       [name listener]]
   [once     [name listener]]
   [emit     [name] {:vargs args}]
   [off      [name listener]]
   [removeAllListeners [name]]
   [listenerCount [name]]
   [listeners [name]]])

(h/template-entries [l/tmpl-macro {:base "Signer"
                                   :inst "signer"
                                   :tag "js"}]
  [;; Accounts
   [connect     [provider]]
   [getAddress  [] {:optional [provider]}]
   [signMessage  [message]]
   [signMessage  [message]]
   [sendTransaction  [tx]]
   [checkTransaction  [tx]]
   [populateTransaction  [tx]]])

(h/template-entries [l/tmpl-macro {:base "ContractFactory"
                                   :inst "factory"
                                   :tag "js"}]
  [;; Deploy
   [getDeployTransaction [] {:vargs args}]
   [deploy []  {:vargs args}]])

(defn.js to-bignum-pow10
  "number with base 10 exponent"
  {:added "4.0"}
  [unit]
  (return
   (. ethers
      BigNumber
      (from "10")
      (pow unit))))

(defn.js bn-mul
  "multiplies two bignums together"
  {:added "4.0"}
  [bn x precision]
  (var b1 (-/parseUnits "1" (or precision 24)))
  (var bx (-/parseUnits (j/toString x) (or precision 24)))
  (return
   (. (-/to-bignum bn)
      (mul bx)
      (div b1))))

(defn.js bn-div
  "divides two bignums together"
  {:added "4.0"}
  [bn x precision]
  (var b1 (-/parseUnits "1" (or precision 24)))
  (var bx (-/parseUnits (j/toString x) (or precision 24)))
  (return
   (. (-/to-bignum bn)
      (mul b1)
      (div bx))))

(defn.js to-number
  "converts the bignum to a number"
  {:added "4.0"}
  [value]
  (cond (and value
             (== (. value type)
                 "BigNumber"))
        (:= value (. value hex)))
  (return
   (. ethers BigNumber (from value) (toNumber))))

(defn.js to-number-string
  "converts the bignum to a number string"
  {:added "4.0"}
  [value]
  (cond (and value
             (== (. value type)
                 "BigNumber"))
        (:= value (. value hex)))
  (return
   (. ethers BigNumber (from value) (toString))))

(defn.js new-rpc-provider
  "creates a new rpc provider"
  {:added "4.0"}
  [url]
  (return (new (. ethers providers JsonRpcProvider)
               url)))

(defn.js new-web3-provider
  "creates a new web3 compatible provider"
  {:added "4.0"}
  [proxy]
  (return (new (. ethers providers Web3Provider)
               proxy)))

(defn.js new-wallet
  "creates a new wallet"
  {:added "4.0"}
  [privateKey provider]
  (return (new (. ethers Wallet) privateKey provider)))

(defn.js new-wallet-from-mnemonic
  "creates new wallet from mnemonic"
  {:added "4.0"}
  [mnemonic path wordlist]
  (return (. ethers Wallet (fromMnemonic mnemonic path wordlist))))

(defn.js new-contract
  "creates a new contract"
  {:added "4.0"}
  [address abi signer]
  (return (new (. ethers Contract) address abi signer)))

(defn.js new-contract-factory
  "creates a new contract factory"
  {:added "4.0"}
  [abi bytecode signer]
  (return (new (. ethers ContractFactory) abi bytecode signer)))

(defn.js get-signer
  "gets a signer given url and private key"
  {:added "4.0"}
  [url private-key]
  (var provider (-/new-rpc-provider url))
  (var wallet (-/new-wallet private-key provider))
  (return wallet))

(defn.js get-signer-address
  "gets signer address given url and private key"
  {:added "4.0"}
  [url private-key]
  (var signer (-/get-signer url private-key))
  (return (-/getAddress signer)))

(defn.js send-wei
  "gets wei to account"
  {:added "4.0"}
  [signer to-address amount gas-limit]
  (var tx {:gasLimit (or gas-limit 21000)
           :to to-address
           :value amount})
  (return (-/sendTransaction signer tx)))

(defn.js contract-deploy
  "deploys the contract"
  {:added "4.0"}
  [signer abi bytecode init-args overrides]
  (var factory (-/new-contract-factory abi bytecode signer))
  (return (. factory (deploy (:.. (or init-args []))
                             overrides))))
;;
;;
;;

(defn.js contract-run
  "runs the contract"
  {:added "4.0"}
  [signer address abi fn-name args overrides]
  (var contract (-/new-contract address abi signer))
  (return ((. contract [fn-name])
           (:.. args)
           overrides)))

(defn.js subscribe-event
  "subscribes to events"
  {:added "4.0"}
  [url event-type listener]
  (var provider (-/new-rpc-provider url))
  (. provider (on event-type listener))
  (return (fn [] (. provider (off event-type listener)))))

(defn.js subscribe-once
  "subscribes to single event"
  {:added "4.0"}
  [url event-type listener]
  (var provider (-/new-rpc-provider url))
  (. provider (once event-type listener))
  (return (fn [] (. provider (off event-type listener)))))

