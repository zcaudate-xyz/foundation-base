tahto/runtime/solidity/compile_solc_test.clj:1:(ns tahto.runtime.solidity.compile-solc-test
tahto/runtime/solidity/compile_solc_test.clj:2:  (:require [tahto.runtime.solidity :as s]
tahto/runtime/solidity/compile_solc_test.clj:3:            [tahto.runtime.solidity.client :as client]
tahto/runtime/solidity/compile_solc_test.clj:4:            [tahto.runtime.solidity.compile-common :as compile-common]
tahto/runtime/solidity/compile_solc_test.clj:5:            [tahto.runtime.solidity.compile-solc :as compile]
tahto/runtime/solidity/compile_solc_test.clj:6:            [tahto.runtime.solidity.env-hardhat :as env]
            [tahto.core :as l]
            [std.lib.env]
            [std.make.compile :as make-compile])
  (:use code.test))

(l/script- :solidity
  {:config  {:mode :clean}
tahto/runtime/solidity/compile_solc_test.clj:14:   :require [[tahto.runtime.solidity :as sol]]})

(defn.sol ^{:- [:pure :internal]
            :static/returns [:string :memory]}
  test:hello []
  (return "HELLO WORLD"))

(fact:global
 {:skip     (not (std.lib.env/program-exists? "node"))
  :setup    [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

tahto/runtime/solidity/compile_solc_test.clj:26:^{:refer tahto.runtime.solidity.compile-solc/compile-base-emit :added "4.0"}
(fact "emits solidity given entries and interfaces"

  (compile/compile-base-emit
   [@test:hello]
   [])
  => vector?)

tahto/runtime/solidity/compile_solc_test.clj:34:^{:refer tahto.runtime.solidity.compile-solc/compile-base-code :added "4.0"}
(fact "compiles base code"

  (compile/compile-base-code "function test__hello() pure public returns(string memory) {\n  return \"HELLO WORLD\";\n}"
                             {})
  => string?)

tahto/runtime/solidity/compile_solc_test.clj:41:^{:refer tahto.runtime.solidity.compile-solc/compile-ptr-prep-open-method :added "4.0"}
(fact "opens up a solidity method"
  (with-redefs [l/grammar (fn [& _] {})
                l/emit-entry (fn [& _] "")]
    (compile/compile-ptr-prep-open-method {:form '(defn f [])}))
  => (contains {:form list?}))

tahto/runtime/solidity/compile_solc_test.clj:48:^{:refer tahto.runtime.solidity.compile-solc/compile-ptr-prep :added "4.0"}
(fact "exports a ptr"

  (compile/compile-ptr-prep test:hello)
  => vector?)

tahto/runtime/solidity/compile_solc_test.clj:54:^{:refer tahto.runtime.solidity.compile-solc/compile-ptr-code :added "4.0"}
(fact "compiles the pointer to code"

  (compile/compile-ptr-code test:hello)
  => string?)

tahto/runtime/solidity/compile_solc_test.clj:60:^{:refer tahto.runtime.solidity.compile-solc/compile-module-prep :added "4.0"}
(fact "preps a namespace or map for emit"

  (compile-common/with:open-methods
   (compile/compile-module-prep nil))
  => vector?)

tahto/runtime/solidity/compile_solc_test.clj:67:^{:refer tahto.runtime.solidity.compile-solc/compile-module-code :added "4.0"}
(fact "compiles the contract code"

  (compile-common/with:open-methods
   (compile/compile-module-code nil))
  => string?)

tahto/runtime/solidity/compile_solc_test.clj:74:^{:refer tahto.runtime.solidity.compile-solc/compile-single-sol :added "4.0"}
(fact "compiles a solidity contract"
  (with-redefs [compile/compile-module-code (fn [_] "code")
                make-compile/compile-fullbody (fn [_ _] "full")
                make-compile/compile-out-path (fn [_] "path")
                make-compile/compile-write (fn [_ _] "out")]
    (compile/compile-single-sol {:main {:name "n"}}))
  => "out")

tahto/runtime/solidity/compile_solc_test.clj:83:^{:refer tahto.runtime.solidity.compile-solc/compile-all-sol :added "4.0"}
(fact "compiles multiple solidity contracts"
  (with-redefs [compile/compile-single-sol (fn [_] "file")
                make-compile/compile-summarise (fn [_] "summary")]
    (compile/compile-all-sol {:main [{:name "n"}]}))
  => "summary")

tahto/runtime/solidity/compile_solc_test.clj:90:^{:refer tahto.runtime.solidity.compile-solc/compile-rt-prep :added "4.0"}
(fact "creates a runtime"
  ;; complex setup
  )

tahto/runtime/solidity/compile_solc_test.clj:95:^{:refer tahto.runtime.solidity.compile-solc/compile-rt-eval :added "4.0"}
(fact "evals form in the runtime"
  ;; complex setup
  )

tahto/runtime/solidity/compile_solc_test.clj:100:^{:refer tahto.runtime.solidity.compile-solc/compile-rt-abi :added "4.0"}
(fact "compiles the contract-abi"
  ;; complex setup
  )

tahto/runtime/solidity/compile_solc_test.clj:105:^{:refer tahto.runtime.solidity.compile-solc/compile-all-abi :added "4.0"}
(fact "compiles the abis"
  ;; complex setup
  )

tahto/runtime/solidity/compile_solc_test.clj:110:^{:refer tahto.runtime.solidity.compile-solc/create-base-entry :added "4.0"}
(fact "creates either a pointer or module entry"
  ;; complex setup
  )

tahto/runtime/solidity/compile_solc_test.clj:115:^{:refer tahto.runtime.solidity.compile-solc/create-pointer-entry :added "4.0"}
(fact "creates a pointer entry"
  ;; complex setup
  )

tahto/runtime/solidity/compile_solc_test.clj:120:^{:refer tahto.runtime.solidity.compile-solc/create-module-entry :added "4.0"}
(fact "creates a compiled module contract entry"
  ;; complex setup
  )

tahto/runtime/solidity/compile_solc_test.clj:125:^{:refer tahto.runtime.solidity.compile-solc/create-file-entry :added "4.0"}
(fact "creates a file entry from a solidity source file"
  (with-redefs [compile/create-base-entry (fn [_rt prep _m _tag _name _refresh]
                                            {:tag :file
                                             :name "USDT.sol"
                                             :code (first (prep {:file "resources/assets/rt.solidity/example/USDT.sol"}))})]
    (compile/create-file-entry (l/rt :solidity)
                               {:name "USDT.sol"
                                :file "resources/assets/rt.solidity/example/USDT.sol"}))
  => (contains {:tag :file
                :name "USDT.sol"}))
