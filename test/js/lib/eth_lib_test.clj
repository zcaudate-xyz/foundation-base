(ns js.lib.eth-lib-test
  (:require [rt.solidity :as s]
            [rt.solidity.compile-solc :as compile-solc]
            [rt.solidity.env-ganache :as env-ganache]
            [std.lang :as l]
            [web3.lib.example-counter :as example-counter]
            [xt.lang.common-notify :as notify])
  (:use code.test))

(l/script- :js
   {:runtime :basic
     :require [[xt.lang.common-lib :as k]
                [xt.lang.common-spec :as xt]
                [xt.lang.common-data :as xtd]
                [xt.lang.common-repl :as repl]
                [js.lib.eth-lib :as e :include [:fn]]
                [js.lib.eth-solc :as eth-solc :include [:fn]]
                [web3.lib.example-counter :as example-counter]
             [js.core :as j]]})

(fact:global
  {:setup    [(s/rt:stop-ganache-server)
              (Thread/sleep 1000)
              (s/rt:start-ganache-server)
              (Thread/sleep 3000)
              (do (l/rt:restart)
                  (l/rt:scaffold :js))]
    :teardown [(l/rt:stop)]})

^{:refer js.lib.eth-lib/to-bignum-pow10 :added "4.0" :unchecked true}
(fact "number with base 10 exponent"

  (!.js
   (. (e/to-bignum-pow10 10)
      (toString)))
  => "10000000000")

^{:refer js.lib.eth-lib/bn-mul :added "4.0" :unchecked true}
(fact "multiplies two bignums together"

  (!.js
   (. (e/bn-mul "100000000000000001"
                "10000" 10)
      (toString)))
  => "1000000000000000010000")

^{:refer js.lib.eth-lib/bn-div :added "4.0" :unchecked true}
(fact "divides two bignums together"

  (!.js
   (. (e/bn-div "100000000000000001"
                "10000" 10)
      (toString)))
  => "10000000000000")

^{:refer js.lib.eth-lib/to-number :added "4.0" :unchecked true}
(fact "converts the bignum to a number"

  (!.js
   (e/to-number "1000000001"))
  => 1000000001

  (!.js
   (e/to-number "100000000000000001"))
  => (throws))

^{:refer js.lib.eth-lib/to-number-string :added "4.0" :unchecked true}
(fact "converts the bignum to a number string"

  (!.js
   (e/to-number-string "100000000000000001"))
  => "100000000000000001")

^{:refer js.lib.eth-lib/new-rpc-provider :added "4.0" :unchecked true}
(fact "creates a new rpc provider"

  (j/<!
   (e/getBlockNumber
    (e/new-rpc-provider "http://127.0.0.1:8545")))
  => number?)

^{:refer js.lib.eth-lib/new-web3-provider :added "4.0" :unchecked true}
(fact "creates a new web3 compatible provider")

^{:refer js.lib.eth-lib/new-wallet :added "4.0" :unchecked true}
(fact "creates a new wallet"

  (j/<!
   (e/getAddress
    (e/new-wallet
     (@! (last env-ganache/+default-private-keys+))
     (e/new-rpc-provider "http://127.0.0.1:8545"))))
  => "0x001Dc339B1E9B9D443bcd39F9d5114390Bc43aCD")

^{:refer js.lib.eth-lib/new-wallet-from-mnemonic :added "4.0" :unchecked true}
(fact "creates new wallet from mnemonic"

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

  (set
   (j/<!
     (xtd/obj-keys
      (e/new-contract "0x94e3361495bD110114ac0b6e35Ed75E77E6a6cFA"
                      (@! (:abi +contract+))
                      (e/get-signer "http://127.0.0.1:8545"
                                    (@! (last env-ganache/+default-private-keys+)))))))
  => #{"interface" "filters" "runner" "fallback" "target"})

^{:refer js.lib.eth-lib/new-contract-factory :added "4.0" :unchecked true
  :setup [(def +contract+
            (compile-solc/create-module-entry
             (l/rt :js)
             example-counter/+default-contract+))]}
(fact "creates a new contract factory"

  (j/<!
   (xtd/obj-keys
    (e/new-contract-factory
     (@! (:abi +contract+))
     (@! (:bytecode +contract+))
     (e/get-signer "http://127.0.0.1:8545"
                   (@! (last env-ganache/+default-private-keys+))))))
  => ["interface" "bytecode" "runner"])

^{:refer js.lib.eth-lib/get-signer :added "4.0" :unchecked true}
(fact "gets a signer given url and private key"

  (j/<!
   (xtd/obj-keys
    (e/get-signer "http://127.0.0.1:8545"
                  (@! (last env-ganache/+default-private-keys+)))))
  => ["provider" "address"])

^{:refer js.lib.eth-lib/get-signer-address :added "4.0" :unchecked true}
(fact "gets signer address given url and private key"

  (j/<!
   (e/get-signer-address "http://127.0.0.1:8545"
                         (@! (last env-ganache/+default-private-keys+))))
  => (last env-ganache/+default-addresses-raw+))

^{:refer js.lib.eth-lib/send-wei :added "4.0" :unchecked true}
(fact "gets wei to account"

  (j/<!
   (e/send-wei (e/get-signer "http://127.0.0.1:8545"
                             (@! (last env-ganache/+default-private-keys+)))
               (@! (first env-ganache/+default-addresses+))
               1000000))
  => (contains-in
      {"gasLimit" any
        "chainId" 1337,
        "from" "0x001Dc339B1E9B9D443bcd39F9d5114390Bc43aCD",
        "to" "0x94e3361495bD110114ac0b6e35Ed75E77E6a6cFA"})

  (bigint
   (j/<!
    (e/getBalance (e/new-rpc-provider "http://127.0.0.1:8545")
                  (@! (first env-ganache/+default-addresses+)))
    j/toString))
  => integer?

  (bigint
   (j/<!
    (e/getBalance (e/new-rpc-provider "http://127.0.0.1:8545")
                  (@! (last env-ganache/+default-addresses+)))
    j/toString))
  => integer?)

^{:refer js.lib.eth-lib/contract-deploy :added "4.0" :unchecked true
  :setup [(def +contract+
            (compile-solc/create-module-entry
             (l/rt :js)
             example-counter/+default-contract+))]}
(fact "deploys the contract"

   (j/<!
    (e/contract-deploy (e/get-signer "http://127.0.0.1:8545"
                                     (@! (last env-ganache/+default-private-keys+)))
                       (@! (:abi +contract+))
                       (@! (:bytecode +contract+))
                       []
                       {})
     (fn [m]
       (return (xt/x:get-key m "target"))))
  => string?)

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
               (fn [m]
                 (return (xt/x:get-key m "target")))))]}
(fact "runs the contract"

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
               (fn [m]
                 (return (xt/x:get-key m "target")))))]}
(fact "subscribes to events"

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
               (fn [m]
                 (return (xt/x:get-key m "target")))))]}
(fact "subscribes to single event"

  (!.js
   (do (var unsub
            (e/subscribe-once "http://127.0.0.1:8545"
                              "block"
                              (fn [x]
                                (return x))))
       (var output (xt/x:is-function? unsub))
       (unsub)
       (return output)))
  => true)

(comment
  (!.js
   (k/sort (xtd/obj-keys (. ethers utils))))

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
   (xtd/obj-keys (. ethers ethers)))

  (!.js
   (xtd/obj-keys P))
  )
