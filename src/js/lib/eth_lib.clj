(ns js.lib.eth-lib
  (:require [std.lang :as l]
            [std.lib.foundation :as f]
            [xt.lang.common-notify :as notify])
  (:refer-clojure :exclude [compile]))

(l/script :js
  {:require [[xt.lang.common-lib :as k]
              [xt.lang.common-data :as xtd]
              [js.core :as j]]})

(defn.js get-ethers
  []
  (var root (or (. process env ["PWD"])
                (. process (cwd))))
  (var ethers (or (!:G ethers)
                  (require (+ root "/node_modules/ethers"))))
  (:= (!:G ethers) ethers)
  (return ethers))

(defn.js ^{:arglists '([message, signature])}
  verifyMessage
  [message signature]
  (var ethers (-/get-ethers))
  (return (. ethers (verifyMessage message signature))))

(defn.js ^{:arglists '([s, units])}
  parseUnits
  [s units]
  (var ethers (-/get-ethers))
  (return (. ethers (parseUnits s units))))

(defn.js ^{:arglists '([s, units])}
  formatUnits
  [s units]
  (var ethers (-/get-ethers))
  (return (. ethers (formatUnits s units))))

(defn.js ^{:arglists '([s, units])}
  keccak256
  [s units]
  (var ethers (-/get-ethers))
  (return (. ethers (keccak256 s units))))

(defn.js ^{:arglists '([s, units])}
  ripemd160
  [s units]
  (var ethers (-/get-ethers))
  (return (. ethers (ripemd160 s units))))

(defn.js ^{:arglists '([s, units])}
  mnemonicToSeed
  [phrase]
  (var ethers (-/get-ethers))
  (return (. (. ethers Mnemonic)
             (fromPhrase phrase)
             (computeSeed))))

(defn.js ^{:arglists '([value])}
  to-bignum
  [value]
  (cond (and value
             (. value toBigInt))
        (return (. value (toBigInt)))

        (and value
             (== (. value type)
                 "BigNumber"))
        (:= value (. value hex)))
  (return (BigInt value)))

(f/template-entries [l/tmpl-macro {:base "Provider"
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

(f/template-entries [l/tmpl-macro {:base "Signer"
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

(f/template-entries [l/tmpl-macro {:base "ContractFactory"
                                   :inst "factory"
                                   :tag "js"}]
  [;; Deploy
   [getDeployTransaction [] {:vargs args}]
   [deploy []  {:vargs args}]])

(defn.js to-bignum-pow10
  "number with base 10 exponent"
  {:added "4.0"}
  [unit]
  (return (-/parseUnits "1" unit)))

(defn.js bn-mul
  "multiplies two bignums together"
  {:added "4.0"}
  [bn x precision]
  (var b1 (-/parseUnits "1" (or precision 24)))
  (var bx (-/parseUnits (j/toString x) (or precision 24)))
  (return (/ (* (-/to-bignum bn) bx)
             b1)))

(defn.js bn-div
  "divides two bignums together"
  {:added "4.0"}
  [bn x precision]
  (var b1 (-/parseUnits "1" (or precision 24)))
  (var bx (-/parseUnits (j/toString x) (or precision 24)))
  (return (/ (* (-/to-bignum bn) b1)
             bx)))

(defn.js to-number
  "converts the bignum to a number"
  {:added "4.0"}
  [value]
  (var bn (-/to-bignum value))
  (when (> bn (BigInt (. Number MAX_SAFE_INTEGER)))
    (throw (new Error "Unsafe integer")))
  (return (Number bn)))

(defn.js to-number-string
  "converts the bignum to a number string"
  {:added "4.0"}
  [value]
  (return (. (-/to-bignum value)
             (toString))))

(defn.js new-rpc-provider
  "creates a new rpc provider"
  {:added "4.0"}
  [url]
  (var ethers (-/get-ethers))
  (return (new (. ethers JsonRpcProvider)
               url)))

(defn.js new-web3-provider
  "creates a new web3 compatible provider"
  {:added "4.0"}
  [proxy]
  (var ethers (-/get-ethers))
  (return (new (. ethers BrowserProvider)
               proxy)))

(defn.js new-wallet
  "creates a new wallet"
  {:added "4.0"}
  [privateKey provider]
  (var ethers (-/get-ethers))
  (return (new (. ethers Wallet) privateKey provider)))

(defn.js new-wallet-from-mnemonic
  "creates new wallet from mnemonic"
  {:added "4.0"}
  [mnemonic path wordlist]
  (var ethers (-/get-ethers))
  (return (. ethers Wallet (fromPhrase mnemonic))))

(defn.js new-contract
  "creates a new contract"
  {:added "4.0"}
  [address abi signer]
  (var ethers (-/get-ethers))
  (return (new (. ethers Contract) address abi signer)))

(defn.js new-contract-factory
  "creates a new contract factory"
  {:added "4.0"}
  [abi bytecode signer]
  (var ethers (-/get-ethers))
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
  (var tx {:gasLimit (-/to-bignum (or gas-limit 21000))
           :to to-address
            :value (-/to-bignum amount)})
  (return (. (-/sendTransaction signer tx)
             (then (fn:> [resp]
                     {:hash (. resp hash)
                      :from (. resp from)
                      :to (. resp to)
                      :chainId (-/to-number (. resp chainId))
                      :gasLimit (j/toString (. resp gasLimit))})))))

(defn.js contract-deploy
  "deploys the contract"
  {:added "4.0"}
  [signer abi bytecode init-args overrides]
  (var factory (-/new-contract-factory abi bytecode signer))
  (return (. (. factory (deploy (:.. (or init-args []))
                                 overrides))
             (then (fn:> [contract]
                      (. (. contract (waitForDeployment))
                         (then (fn:> [_]
                                 {:target (. contract target)
                                  :contractAddress (. contract target)}))))))))
;;
;;
;;

(defn.js contract-run
  "runs the contract"
  {:added "4.0"}
  [signer address abi fn-name args overrides]
  (var contract (-/new-contract address abi signer))
  (var normalize
       (fn:> [value]
         (return
          (xtd/tree-walk value
                        (fn:> [o]
                           (return
                            (:? (== "bigint"
                                    (k/type-native o))
                                (j/toString o)
                                (:? (== "BigNumber"
                                        (k/type-native o))
                                    (j/toString o)
                                    o))))
                         k/identity))))
  (var output
       (:? overrides
           ((. contract [fn-name])
            (:.. (or args []))
            overrides)
           ((. contract [fn-name])
            (:.. (or args [])))))
  (return (. output
             (then (fn:> [res]
                     (:? (and (k/is-object? res)
                              (. res wait))
                         (. (. res (wait))
                            (then normalize))
                         (normalize res)))))))

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
  (var handler
       (fn [arg]
         (. provider (off event-type handler))
         (return (listener arg))))
  (. provider (on event-type handler))
  (return (fn [] (. provider (off event-type handler)))))
