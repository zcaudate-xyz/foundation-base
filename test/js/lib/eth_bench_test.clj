(ns js.lib.eth-bench-test
  (:require [rt.solidity :as solidity]
            [rt.solidity.compile-solc :as compile-solc]
            [rt.solidity.env-ganache :as env-ganache]
            [std.lang :as l]
            [web3.lib.example-counter :as example-counter])
  (:use code.test))

(l/script- :js
   {:runtime :basic
    :require [[xt.lang.common-lib :as k]
               [xt.lang.common-repl :as repl]
               [js.lib.eth-bench :as e :include [:fn]]
               [js.lib.eth-solc :as eth-solc :include [:fn]]
               [js.core :as j]]})

(fact:global
 {:setup    [(solidity/rt:stop-ganache-server)
             (Thread/sleep 1000)
             (solidity/rt:start-ganache-server)
             (Thread/sleep 3000)
             (l/rt:restart)
              (l/rt:scaffold :js)]
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
  => (contains-in {"status" false,
                   "data" map?}))

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
               (fn [m]
                 (return (k/get-key m "contractAddress")))))]}
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
               (fn [m]
                 (return (k/get-key m "contractAddress")))))
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
