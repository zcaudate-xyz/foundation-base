(ns js.lib.eth-bench-test
  (:use code.test)
  (:require [std.lib :as h]
            [std.lang :as l]
            [rt.solidity :as solidity]
            [rt.solidity.env-ganache :as env-ganache]
            [rt.solidity.compile-solc :as compile-solc]
            [web3.lib.example-counter :as example-counter]))

(l/script- :js
  {:runtime :basic
   :require [[xt.lang.base-lib :as k]
             [xt.lang.base-repl :as repl]
             [js.lib.eth-bench :as e :include [:fn]]
             [js.lib.eth-solc :as eth-solc :include [:fn]]
             [js.core :as j]]})

(fact:global
 {:setup    [(solidity/rt:start-ganache-server)
             (l/rt:restart)
             (l/rt:scaffold :js)
             (!.js
              (:= solc (require "solc")))]
  :teardown [(l/rt:stop)]})

^{:refer js.lib.eth-bench/send-wei :added "4.0" :unchecked true}
(fact "sends currency for bench")

^{:refer js.lib.eth-bench/contract-deploy :added "4.0" :unchecked true
  :setup [(def +contract+
            (compile-solc/create-module-entry
             (l/rt :js)
             example-counter/+default-contract+))]}
(fact "deploys the contract"
  ^:hidden

  (j/<!
   (e/contract-deploy "http://127.0.0.1:8545"
                      (@! (last env-ganache/+default-private-keys+))
                      (@! (:abi +contract+))
                      (@! (:bytecode +contract+))
                      []
                      {}))
  => (contains-in
      {"status" true,
       "contractAddress" string?})

  (j/<!
   (e/contract-deploy "http://127.0.0.1:8545"
                      (@! (last env-ganache/+default-private-keys+))
                      (@! (:abi +contract+))
                      (@! (:bytecode +contract+))
                      [1 2 3]
                      {}))
  => {"status" false,
      "data"
      {"count" 4,
       "expectedCount" 0,
       "reason" "too many arguments:  in Contract constructor",
       "code" "UNEXPECTED_ARGUMENT"}})

^{:refer js.lib.eth-bench/contract-run :added "4.0" :unchecked true
  :setup [(def +contract+
            (compile-solc/create-module-entry
             (l/rt :js)
             example-counter/+default-contract+))
          (def +address+
            (j/<!
             (e/contract-deploy "http://127.0.0.1:8545"
                      (@! (last env-ganache/+default-private-keys+))
                      (@! (:abi +contract+))
                      (@! (:bytecode +contract+))
                      []
                      {})
             (k/key-fn "contractAddress")))]}
(fact "runs the contract given address and arguments"
  ^:hidden

  (j/<! (e/contract-run "http://127.0.0.1:8545"
                        (@! (last env-ganache/+default-private-keys+))
                        (@! +address+)
                        (@! (:abi +contract+))
                        "m__inc_both"
                        []
                        nil))
  => map?

  (j/<! (e/contract-run "http://127.0.0.1:8545"
                        (@! (last env-ganache/+default-private-keys+))
                        (@! +address+)
                        (@! (:abi +contract+))
                        "g__Counter0"
                        []
                        nil)
        k/to-number)
  => number?)

^{:refer js.lib.eth-bench/get-past-events :added "4.0" :unchecked true
  :setup [(def +contract+
            (compile-solc/create-module-entry
             (l/rt :js)
             example-counter/+default-contract+))
          (def +address+
            (j/<!
             (e/contract-deploy "http://127.0.0.1:8545"
                      (@! (last env-ganache/+default-private-keys+))
                      (@! (:abi +contract+))
                      (@! (:bytecode +contract+))
                      []
                      {})
             (k/key-fn "contractAddress")))
          (j/<! (e/contract-run "http://127.0.0.1:8545"
                        (@! (last env-ganache/+default-private-keys+))
                        (@! +address+)
                        (@! (:abi +contract+))
                        "m__inc_both"
                        []
                        nil))]}
(fact "gets all past events"
  ^:hidden
  
  (j/<! (e/get-past-events "http://127.0.0.1:8545"
                           (@! +address+)
                           (@! (:abi +contract+))
                           "CounterLog"
                           {}))
  => vector?)
