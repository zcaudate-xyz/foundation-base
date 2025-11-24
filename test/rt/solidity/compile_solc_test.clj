(ns rt.solidity.compile-solc-test
  (:use code.test)
  (:require [rt.solidity.client :as client]
            [rt.solidity.compile-common :as compile-common]
            [rt.solidity.compile-solc :as compile]
            [rt.solidity.env-ganache :as env]
            [rt.solidity :as s]
            [std.lang :as l]
            [std.lib :as h]
            [std.make.compile :as make-compile]))

(l/script- :solidity
  {:config  {:mode :clean}
   :require [[rt.solidity :as s]]})

(defn.sol ^{:- [:pure :internal]
            :static/returns [:string :memory]}
  test:hello []
  (return "HELLO WORLD"))

(fact:global
 {:setup    [(l/rt:restart)]
  :teardown [(l/rt:stop)]})

^{:refer rt.solidity.compile-solc/compile-base-emit :added "4.0"}
(fact "emits solidity given entries and interfaces"
  ^:hidden
  
  (compile/compile-base-emit
   [@test:hello]
   [])
  => vector?)
  
^{:refer rt.solidity.compile-solc/compile-base-code :added "4.0"}
(fact "compiles base code"
  ^:hidden

  (compile/compile-base-code "function test__hello() pure public returns(string memory) {\n  return \"HELLO WORLD\";\n}"
                             {})
  => string?)

^{:refer rt.solidity.compile-solc/compile-ptr-prep-open-method :added "4.0"}
(fact "opens up a solidity method"
  (with-redefs [l/grammar (fn [& _] {})
                l/emit-entry (fn [& _] "")]
    (compile/compile-ptr-prep-open-method {:form '(defn f [])}))
  => (contains {:form list?}))

^{:refer rt.solidity.compile-solc/compile-ptr-prep :added "4.0"}
(fact "exports a ptr"
  ^:hidden

  (compile/compile-ptr-prep test:hello)
  => vector?)

^{:refer rt.solidity.compile-solc/compile-ptr-code :added "4.0"}
(fact "compiles the pointer to code"
  ^:hidden

  (compile/compile-ptr-code test:hello)
  => string?)

^{:refer rt.solidity.compile-solc/compile-module-prep :added "4.0"}
(fact "preps a namespace or map for emit"
  ^:hidden

  (compile-common/with:open-methods
   (compile/compile-module-prep nil))
  => vector?)

^{:refer rt.solidity.compile-solc/compile-module-code :added "4.0"}
(fact "compiles the contract code"
  ^:hidden

  (compile-common/with:open-methods
   (compile/compile-module-code nil))
  => string?)

^{:refer rt.solidity.compile-solc/compile-single-sol :added "4.0"}
(fact "compiles a solidity contract"
  (with-redefs [compile/compile-module-code (fn [_] "code")
                make-compile/compile-fullbody (fn [_ _] "full")
                make-compile/compile-out-path (fn [_] "path")
                make-compile/compile-write (fn [_ _] "out")]
    (compile/compile-single-sol {:main {:name "n"}}))
  => "out")

^{:refer rt.solidity.compile-solc/compile-all-sol :added "4.0"}
(fact "compiles multiple solidity contracts"
  (with-redefs [compile/compile-single-sol (fn [_] "file")
                make-compile/compile-summarise (fn [_] "summary")]
    (compile/compile-all-sol {:main [{:name "n"}]}))
  => "summary")

^{:refer rt.solidity.compile-solc/compile-rt-prep :added "4.0"}
(fact "creates a runtime"
  ;; complex setup
  )

^{:refer rt.solidity.compile-solc/compile-rt-eval :added "4.0"}
(fact "evals form in the runtime"
  ;; complex setup
  )

^{:refer rt.solidity.compile-solc/compile-rt-abi :added "4.0"}
(fact "compiles the contract-abi"
  ;; complex setup
  )

^{:refer rt.solidity.compile-solc/compile-all-abi :added "4.0"}
(fact "compiles the abis"
  ;; complex setup
  )

^{:refer rt.solidity.compile-solc/create-base-entry :added "4.0"}
(fact "creates either a pointer or module entry"
  ;; complex setup
  )

^{:refer rt.solidity.compile-solc/create-pointer-entry :added "4.0"}
(fact "creates a pointer entry"
  ;; complex setup
  )

^{:refer rt.solidity.compile-solc/create-module-entry :added "4.0"}
(fact "creates a compiled module contract entry"
  ;; complex setup
  )

^{:refer rt.solidity.compile-solc/create-file-entry :added "4.0"}
(comment "creates a file entry"
  
  (compile/create-file-entry {}
                             {:name "USDT.sol"
                              :file "resources/assets/rt.solidity/example/USDT.sol"}))
