(ns std.contract.binding-test
  (:use code.test)
  (:require [std.contract.binding :refer :all]
            [std.contract.type :as type])
  (:refer-clojure :exclude [bound?]))

(defn -add-
  ([x y]
   (+ x y)))

(def -contract-
  (contract #'-add- [] nil))

^{:refer std.contract.binding/contract-info :added "3.0"}
(fact "returns the contract info"

  (-> (contract #'-add- [] nil)
      (contract-info))
  => map?)

^{:refer std.contract.binding/contract-invoke :added "3.0"}
(fact "invokes the contract"

  (-> (contract #'-add- [] nil)
      (contract-invoke 1 2))
  => 3)

^{:refer std.contract.binding/map->Contract :added "3.0" :adopt true}
(fact "definition of contract type"
  (map->Contract {:var #'-add- :function -add- :specs {}}) => contract?)

^{:refer std.contract.binding/contract? :added "3.0"}
(fact "checks if object is a contract"
  (contract? -contract-) => true
  (contract? "foo") => false)

^{:refer std.contract.binding/contract-var? :added "3.0"}
(fact "checks if var contains a contract"

  (contract-var? #'-add-)
  => false

  (contract-var? #'-contract-)
  => true)

^{:refer std.contract.binding/bound? :added "3.0"}
(fact "checks if contract is bound to var"

  (bound? #'-add-)
  => false

  (bound? #'-contract-)
  => false

  (bound? -contract-)
  => false)

^{:refer std.contract.binding/unbind :added "3.0"}
(fact "unbinds the contract to its var"

  (do (unbind #'-minus-)
      (bound? #'-minus-))
  => false)

^{:refer std.contract.binding/bind :added "3.0"}
(fact "binds the contract to its var"

  (defn -minus-
    ([x y]
     (- x y)))

  (do (bind (contract #'-minus- []))
      (bound? #'-minus-))
  => true)

^{:refer std.contract.binding/parse-arg :added "3.0"}
(fact "parses an input/output arg"

  (type/defspec <number> number?)

  (parse-arg {<number> :strict})
  => {:spec <number> :opts :strict}

  (parse-arg <number>)
  => {:spec <number> :opts nil})

^{:refer std.contract.binding/contract :added "3.0"}
(fact "defines a contract given var and arguments"
  (contract #'-add- []) => contract?)

^{:refer std.contract.binding/defcontract :added "3.0"}
(fact "defines a contract"

  (type/defspec <int> integer?)

  (defcontract -add-
    :inputs [<int> <int>])

  (-add- 0.5 0.5)
  => (throws))
