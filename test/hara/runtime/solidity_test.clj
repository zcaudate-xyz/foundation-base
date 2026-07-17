(ns hara.runtime.solidity-test
  (:require [hara.runtime.solidity :as s]
            [hara.runtime.solidity.client :as client]
            [hara.runtime.solidity.compile-common :as compile-common]
            [hara.runtime.solidity.compile-solc :as compile-solc]
            [hara.runtime.solidity.env-hardhat :as env]
            [hara.lang :as l]
            [std.lib.env :as senv])
  (:use code.test))

(l/script- :solidity
  {:runtime :web3
   :require [[hara.runtime.solidity :as sol]]
   :static  {:contract ["Hello"]}
   :test-mode true})

(fact:global
 {:skip     (not (senv/program-exists? "node"))
  :setup    [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer hara.runtime.solidity/exec-rt-web3 :added "4.0"}
(fact "helper function for executing a command via node"
  (with-redefs [l/rt (fn [_] {:runtime :web3})
                client/stop-web3 (fn [_] nil)]
    (s/exec-rt-web3 nil (fn [_] :ok)))
  => :ok)

^{:refer hara.runtime.solidity/rt:print :added "4.0"}
(fact "prints module contract code with line numbers"
  (let [calls (atom [])]
    (with-redefs [compile-solc/compile-module-code (fn [m]
                                                      (swap! calls conj [:module m])
                                                      "contract Test {}")
                  senv/pl-add-lines (fn [body & _]
                                      (swap! calls conj [:pl body])
                                      body)]
      (s/rt:print {:name "Test"})
      @calls))
  => [[:module {:name "Test"}] [:pl "contract Test {}"]])

^{:refer hara.runtime.solidity/rt:print :added "4.0"
  :id test-rt-print-pointer-contract}
(fact "prints pointer contract code when :module and :id are supplied"
  (let [calls (atom [])]
    (with-redefs [compile-solc/compile-ptr-code (fn [m]
                                                  (swap! calls conj [:ptr m])
                                                  "contract Ptr {}")
                  senv/pl-add-lines (fn [body & _]
                                      (swap! calls conj [:pl body])
                                      body)]
      (s/rt:print {:module "Hello" :id "main"})
      @calls))
  => [[:ptr {:module "Hello" :id "main"}] [:pl "contract Ptr {}"]])

^{:refer hara.runtime.solidity/rt:print :added "4.0"
  :id test-rt-print-no-lines-argument}
(fact "suppresses line numbers via second argument"
  (let [calls (atom [])]
    (with-redefs [compile-solc/compile-module-code (fn [_] "line1\nline2")
                  senv/p (fn [& args]
                           (swap! calls conj args))]
      (s/rt:print {} true)
      @calls))
  => [["line1\nline2"]])

^{:refer hara.runtime.solidity/rt:print :added "4.0"
  :id test-rt-print-no-lines-option}
(fact "suppresses line numbers via :no-lines option"
  (let [calls (atom [])]
    (with-redefs [compile-solc/compile-module-code (fn [_] "line1\nline2")
                  senv/p (fn [& args]
                           (swap! calls conj args))]
      (s/rt:print {:no-lines true})
      @calls))
  => [["line1\nline2"]])

^{:refer hara.runtime.solidity/rt:deploy-ptr :added "4.0"}
(fact "deploys a ptr a contract"
  ;; Requires web3
  )

^{:refer hara.runtime.solidity/rt:deploy :added "4.0"}
(fact "deploys current namespace as contract"
  ;; Requires web3
  )

^{:refer hara.runtime.solidity/rt:contract :added "4.0"}
(fact "gets the contract"
  ;; Requires web3
  )

^{:refer hara.runtime.solidity/rt:bytecode-size :added "4.0"}
(fact "gets the bytecode size"
  ;; Requires web3
  )

(comment
  (s/rt-get-contract-address)
  (s/rt-ge))
