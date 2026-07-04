(ns js.lib.eth-bench-test
  (:require [hara.runtime.solidity :as solidity]
            [hara.runtime.solidity.compile-solc :as compile-solc]
            [hara.runtime.solidity.env-hardhat :as env-hardhat]
            [hara.lang :as l]
            [web3.lib.example-counter :as example-counter]
            [xt.lang.common-notify :as notify])
  (:use code.test))

(l/script- :js
   {:runtime :basic
     :require [[xt.lang.spec-base :as xt]
                [xt.lang.common-lib :as k]
                [xt.lang.common-repl :as repl]
                [js.lib.eth-bench :as e :include [:fn]]
                [js.lib.eth-solc :as eth-solc :include [:fn]]]})

(fact:global
 {:setup    [(solidity/rt:stop-hardhat-server)
             (Thread/sleep 1000)
             (solidity/rt:start-hardhat-server)
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

  (notify/wait-on :js
    (. (e/contract-deploy "http://127.0.0.1:8545"
                          (@! (last env-hardhat/+default-private-keys+))
                          (@! (:abi +contract+))
                          (@! (:bytecode +contract+))
                          []
                          {})
       (then (fn [result]
               (repl/notify result)))))
  => (contains-in
      {"status" true,
        "contractAddress" string?})

  (notify/wait-on :js
    (. (e/contract-deploy "http://127.0.0.1:8545"
                          (@! (last env-hardhat/+default-private-keys+))
                          (@! (:abi +contract+))
                          (@! (:bytecode +contract+))
                          [1 2 3]
                          {})
       (then (fn [result]
               (repl/notify result)))))
  => (contains-in {"status" false,
                   "data" map?}))

^{:refer js.lib.eth-bench/contract-run :added "4.0" :unchecked true
  :setup [(def +contract+
            (compile-solc/create-module-entry
             (l/rt :js)
             example-counter/+default-contract+))
          (def +address+
             (notify/wait-on :js
               (. (e/contract-deploy "http://127.0.0.1:8545"
                                     (@! (last env-hardhat/+default-private-keys+))
                                     (@! (:abi +contract+))
                                     (@! (:bytecode +contract+))
                                     []
                                     {})
                  (then (fn [m]
                          (repl/notify (xt/x:get-key m "contractAddress")))))))]}
(fact "runs the contract given address and arguments"

  (notify/wait-on :js
    (. (e/contract-run "http://127.0.0.1:8545"
                       (@! (last env-hardhat/+default-private-keys+))
                       (@! +address+)
                       (@! (:abi +contract+))
                       "m__inc_both"
                       []
                       nil)
       (then (fn [result]
               (repl/notify result)))))
  => map?

  (notify/wait-on :js
    (. (e/contract-run "http://127.0.0.1:8545"
                       (@! (last env-hardhat/+default-private-keys+))
                       (@! +address+)
                       (@! (:abi +contract+))
                       "g__Counter0"
                       []
                       nil)
       (then (fn [result]
               (repl/notify (k/to-number result))))))
  => number?)

^{:refer js.lib.eth-bench/get-past-events :added "4.0" :unchecked true
  :setup [(def +contract+
            (compile-solc/create-module-entry
             (l/rt :js)
             example-counter/+default-contract+))
          (def +address+
             (notify/wait-on :js
               (. (e/contract-deploy "http://127.0.0.1:8545"
                                     (@! (last env-hardhat/+default-private-keys+))
                                     (@! (:abi +contract+))
                                     (@! (:bytecode +contract+))
                                     []
                                     {})
                  (then (fn [m]
                          (repl/notify (xt/x:get-key m "contractAddress")))))))
          (notify/wait-on :js
            (. (e/contract-run "http://127.0.0.1:8545"
                               (@! (last env-hardhat/+default-private-keys+))
                               (@! +address+)
                               (@! (:abi +contract+))
                               "m__inc_both"
                               []
                               nil)
               (then (fn [result]
                       (repl/notify result)))))]}
(fact "gets all past events"

  (notify/wait-on :js
    (. (e/get-past-events "http://127.0.0.1:8545"
                          (@! +address+)
                          (@! (:abi +contract+))
                          "CounterLog"
                          {})
       (then (fn [result]
               (repl/notify result)))))
  => vector?)
