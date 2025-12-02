(ns js.lib.eth-lib-test
  (:use code.test)
  (:require [std.lib :as h]
            [std.lang :as l]
            [xt.lang.base-notify :as notify]
            [rt.solidity :as s]
            [rt.solidity.env-ganache :as env-ganache]
            [rt.solidity.compile-solc :as compile-solc]
            [web3.lib.example-counter :as example-counter]))

(l/script- :js
  {:runtime :basic
   :require [[xt.lang.base-lib :as k]
             [xt.lang.base-repl :as repl]
             [js.lib.eth-lib :as e :include [:fn]]
             [js.lib.eth-solc :as eth-solc :include [:fn]]
             [web3.lib.example-counter :as example-counter]
             [js.core :as j]]})

(fact:global
 {:setup    [(s/rt:stop-ganache-server)
             (Thread/sleep 1000)
             (s/rt:start-ganache-server)
             (Thread/sleep 500)
             (do (l/rt:restart)
                 (l/rt:scaffold :js))
             (!.js
              (:= solc (require "solc")))]
  :teardown [(l/rt:stop)]})

^{:refer js.lib.eth-lib/to-bignum-pow10 :added "4.0" :unchecked true}
(fact "number with base 10 exponent"
  ^:hidden

  (!.js
   (. (e/to-bignum-pow10 10)
      (toString)))
  => "10000000000")

^{:refer js.lib.eth-lib/bn-mul :added "4.0" :unchecked true}
(fact "multiplies two bignums together"
  ^:hidden
  
  (!.js
   (. (e/bn-mul "100000000000000001"
                "10000" 10)
      (toString)))
  => "1000000000000000010000")

^{:refer js.lib.eth-lib/bn-div :added "4.0" :unchecked true}
(fact "divides two bignums together"
  ^:hidden
  
  (!.js
   (. (e/bn-div "100000000000000001"
                "10000" 10)
      (toString)))
  => "10000000000000")

^{:refer js.lib.eth-lib/to-number :added "4.0" :unchecked true}
(fact "converts the bignum to a number"
  ^:hidden

  (!.js
   (e/to-number "1000000001"))
  => 1000000001
  
  (!.js
   (e/to-number "100000000000000001"))
  => (throws))

^{:refer js.lib.eth-lib/to-number-string :added "4.0" :unchecked true}
(fact "converts the bignum to a number string"
  ^:hidden

  (!.js
   (e/to-number-string "100000000000000001"))
  => "100000000000000001")

^{:refer js.lib.eth-lib/new-rpc-provider :added "4.0" :unchecked true}
(fact "creates a new rpc provider"
  ^:hidden

  (j/<!
   (e/getBlockNumber
    (e/new-rpc-provider "http://127.0.0.1:8545")))
  => number?)

^{:refer js.lib.eth-lib/new-web3-provider :added "4.0" :unchecked true}
(fact "creates a new web3 compatible provider")

^{:refer js.lib.eth-lib/new-wallet :added "4.0" :unchecked true}
(fact "creates a new wallet"
  ^:hidden
  
  (j/<!
   (e/getAddress
    (e/new-wallet
     (@! (last env-ganache/+default-private-keys+))
     (e/new-rpc-provider "http://127.0.0.1:8545"))))
  => "0x001Dc339B1E9B9D443bcd39F9d5114390Bc43aCD")

^{:refer js.lib.eth-lib/new-wallet-from-mnemonic :added "4.0" :unchecked true}
(fact "creates new wallet from mnemonic"
  ^:hidden

  (j/<!
   (e/getAddress
    (e/new-wallet-from-mnemonic
     "taxi dash nation raw first art ticket more useful mosquito include true")))
  => "0x94e3361495bD110114ac0b6e35Ed75E77E6a6cFA")

^{:refer js.lib.eth-lib/new-contract :added "4.0" :unchecked true
  :setup [(def +contract+
            (compile-solc/create-module-entry
             (l/rt :js)
             example-counter/+default-contract+))]}
(fact "creates a new contract"
  ^:hidden
  
  (set
   (j/<!
    (k/obj-keys
     (e/new-contract "0x94e3361495bD110114ac0b6e35Ed75E77E6a6cFA"
                     (@! (:abi +contract+))
                     (e/get-signer "http://127.0.0.1:8545"
                                   (@! (last env-ganache/+default-private-keys+)))))))
  => #{"m__get_counter1"
       "_runningEvents"
       "m__inc_counter0()"
       "m__dec_counter1()"
       "signer"
       "interface"
       "m__inc_counter1()"
       "m__get_counter1()"
       "resolvedAddress"
       "m__add_both"
       "m__inc_both()"
       "estimateGas"
       "m__inc_both"
       "filters"
       "provider"
       "m__inc_counter1"
       "address"
       "m__dec_counter0"
       "g__Counter0"
       "functions"
       "g__Counter1"
       "callStatic"
       "populateTransaction"
       "g__Counter1()"
       "_wrappedEmits"
       "m__inc_counter0"
       "m__get_counter0()"
       "m__dec_counter1"
       "g__Counter0()"
       "m__dec_counter0()"
       "m__get_counter0"
       "m__add_both(uint256)"})

^{:refer js.lib.eth-lib/new-contract-factory :added "4.0" :unchecked true
  :setup [(def +contract+
            (compile-solc/create-module-entry
             (l/rt :js)
             example-counter/+default-contract+))]}
(fact "creates a new contract factory"
  ^:hidden
  
  (j/<!
   (k/obj-keys
    (e/new-contract-factory
     (@! (:abi +contract+))
     (@! (:bytecode +contract+))
     (e/get-signer "http://127.0.0.1:8545"
                   (@! (last env-ganache/+default-private-keys+))))))
  => ["bytecode" "interface" "signer"])

^{:refer js.lib.eth-lib/get-signer :added "4.0" :unchecked true}
(fact "gets a signer given url and private key"
  ^:hidden
  
  (j/<!
   (k/obj-keys
    (e/get-signer "http://127.0.0.1:8545"
                  (@! (last env-ganache/+default-private-keys+)))))
  => ["_isSigner" "_signingKey" "_mnemonic" "address" "provider"])

^{:refer js.lib.eth-lib/get-signer-address :added "4.0" :unchecked true}
(fact "gets signer address given url and private key"
  ^:hidden

  (j/<!
   (e/get-signer-address "http://127.0.0.1:8545"
                         (@! (last env-ganache/+default-private-keys+))))
  => (last env-ganache/+default-addresses-raw+))

^{:refer js.lib.eth-lib/send-wei :added "4.0" :unchecked true}
(fact "gets wei to account"
  ^:hidden
  
  (j/<!
   (e/send-wei (e/get-signer "http://127.0.0.1:8545"
                             (@! (last env-ganache/+default-private-keys+)))
               (@! (first env-ganache/+default-addresses+))
               1000000))
  => (contains-in
      {"gasLimit" {"hex" "0x5208", "type" "BigNumber"},
       "chainId" 1337,
       "gasPrice" nil,
       "confirmations" 0,
       "from" "0x001Dc339B1E9B9D443bcd39F9d5114390Bc43aCD",
       "to" "0x94e3361495bD110114ac0b6e35Ed75E77E6a6cFA"})

  (bigint
   (j/<!
    (e/getBalance (e/new-rpc-provider "http://127.0.0.1:8545")
                  (@! (first env-ganache/+default-addresses+)))
    j/toString))
  => integer?
  
  (j/<!
   (e/getBalance (e/new-rpc-provider "http://127.0.0.1:8545")
                 (@! (last env-ganache/+default-addresses+)))
   k/to-number)
  => integer?)

^{:refer js.lib.eth-lib/contract-deploy :added "4.0" :unchecked true
  :setup [(def +contract+
            (compile-solc/create-module-entry
             (l/rt :js)
             example-counter/+default-contract+))]}
(fact "deploys the contract"
  ^:hidden
  
  (j/<!
   (e/contract-deploy (e/get-signer "http://127.0.0.1:8545"
                                    (@! (last env-ganache/+default-private-keys+)))
                      (@! (:abi +contract+))
                      (@! (:bytecode +contract+))
                      []
                      {})
   (k/key-fn "deployTransaction"))
  => map?)

^{:refer js.lib.eth-lib/contract-run :added "4.0" :unchecked true
  :setup [(def +contract+
            (compile-solc/create-module-entry
             (l/rt :js)
             example-counter/+default-contract+))
          (def +address+
            (j/<!
             (e/contract-deploy (e/get-signer "http://127.0.0.1:8545"
                                              (@! (last env-ganache/+default-private-keys+)))
                                (@! (:abi +contract+))
                                (@! (:bytecode +contract+))
                                []
                                {})
             (k/key-fn "address")))]}
(fact "runs the contract"
  ^:hidden
  
  (j/<! (e/contract-run (e/get-signer "http://127.0.0.1:8545"
                                      (@! (last env-ganache/+default-private-keys+)))
                        (@! +address+)
                        (@! (:abi +contract+))
                        "m__inc_both"
                        []
                        nil))

  (j/<! (e/contract-run (e/get-signer "http://127.0.0.1:8545"
                                      (@! (last env-ganache/+default-private-keys+)))
                        (@! +address+)
                        (@! (:abi +contract+))
                        "g__Counter0"
                        []
                        nil)
        k/to-number)
  => integer?)

^{:refer js.lib.eth-lib/subscribe-event :added "4.0" :unchecked true
  :setup [(def +contract+
            (compile-solc/create-module-entry
             (l/rt :js)
             example-counter/+default-contract+))
          (def +address+
            (j/<!
             (e/contract-deploy (e/get-signer "http://127.0.0.1:8545"
                                              (@! (last env-ganache/+default-private-keys+)))
                                (@! (:abi +contract+))
                                (@! (:bytecode +contract+))
                                []
                                {})
             (k/key-fn "address")))]}
(fact "subscribes to events"
  ^:hidden

  (notify/wait-on [:js 5000]
    (. (e/getBlockNumber (e/new-rpc-provider  "http://127.0.0.1:8545"))
       (then (fn:> [block]
               (e/subscribe-event "http://127.0.0.1:8545"
                                  "block"
                                  (fn [b]
                                   (when (not= block b)
                                     (repl/notify [block b]))))))
       (then (fn:>
               (e/contract-run (e/get-signer "http://127.0.0.1:8545"
                                             (@! (last env-ganache/+default-private-keys+)))
                               (@! +address+)
                               (@! (:abi +contract+))
                               "m__inc_both"
                               []
                               nil)))
       (then (fn:>
               (e/contract-run (e/get-signer "http://127.0.0.1:8545"
                                             (@! (last env-ganache/+default-private-keys+)))
                               (@! +address+)
                               (@! (:abi +contract+))
                               "m__inc_both"
                               []
                               nil)))))
  => (contains-in [number? number?]))

^{:refer js.lib.eth-lib/subscribe-once :added "4.0" :unchecked true
  :setup [(def +contract+
            (compile-solc/create-module-entry
             (l/rt :js)
             example-counter/+default-contract+))
          (def +address+
            (j/<!
             (e/contract-deploy (e/get-signer "http://127.0.0.1:8545"
                                              (@! (last env-ganache/+default-private-keys+)))
                                (@! (:abi +contract+))
                                (@! (:bytecode +contract+))
                                []
                                {})
             (k/key-fn "address")))]}
(fact "subscribes to single event"
  ^:hidden

  (notify/wait-on :js
    (e/subscribe-once "http://127.0.0.1:8545"
                      "pending"
                      (repl/>notify))
    (e/contract-run (e/get-signer "http://127.0.0.1:8545"
                                  (@! (last env-ganache/+default-private-keys+)))
                    (@! +address+)
                    (@! (:abi +contract+))
                    "m__inc_both"
                    []
                    nil))
  => map?)

(comment
  (!.js
   (k/sort (k/obj-keys (. ethers utils))))

  (e/parseUnits "1.234"
                8)
  (!.js
   (. ethers utils
      (parseUnits "1.234"
                  "8")))
  (!.js
   (* (e/to-bignum
       (e/to-bignum "100000"))
      1.2))
  (!.js
   (. '((:- "10000000000000000000"))
      (toFixed 0))))

(comment
  (new ethers)

  (!.js
   (:= (!:G P)
       ))
  
  (!.js (. P (getSigner)))
  
  (j/<! (e/getBlockNumber P))
  
  
  (!.js
   (k/obj-keys (. ethers ethers)))

  (!.js
   (k/obj-keys P))
  )
