(ns rt.solidity.compile-common-test
  (:use code.test)
  (:require [rt.solidity.compile-common :refer :all]
            [std.lang :as l]))

^{:refer rt.solidity.compile-common/clear-compiled :added "4.0"}
(fact "clears all compiled entries"
  (clear-compiled)
  @+compiled+ => {:module {} :entry {}})

^{:refer rt.solidity.compile-common/with:open-methods :added "4.0"}
(fact "forces the open methods flag"
  (with:open-methods *open-methods*) => true)

^{:refer rt.solidity.compile-common/with:closed-methods :added "4.0"}
(fact "turns off the open methods flag"
  (with:closed-methods *open-methods*) => false)

^{:refer rt.solidity.compile-common/with:suppress-errors :added "4.0"}
(fact "suppresses printing of errors"
  (with:suppress-errors *suppress-errors*) => true)

^{:refer rt.solidity.compile-common/with:stringify :added "4.0"}
(fact "stringifies the output"
  (with:stringify *stringify*) => true)

^{:refer rt.solidity.compile-common/with:temp :added "4.0"}
(fact "deploy and run temp contract"
  (with:temp *temp*) => true)

^{:refer rt.solidity.compile-common/with:clean :added "4.0"}
(fact "with clean flag"
  (with:clean [true] *clean*) => true)

^{:refer rt.solidity.compile-common/with:url :added "4.0"}
(fact "overrides api url"
  (with:url ["http://localhost:8545"] *url*) => "http://localhost:8545")

^{:refer rt.solidity.compile-common/with:caller-address :added "4.0"}
(fact "overrides caller address"
  (with:caller-address ["0x123"] *caller-address*) => "0x123")

^{:refer rt.solidity.compile-common/with:caller-private-key :added "4.0"}
(fact "overrides caller private key"
  (with:caller-private-key ["key"] *caller-private-key*) => "key")

^{:refer rt.solidity.compile-common/with:caller-payment :added "4.0"}
(fact "overrides the caller payment"
  (with:caller-payment [100] *caller-payment*) => 100)

^{:refer rt.solidity.compile-common/with:contract-address :added "4.0"}
(fact "overrides contract address"
  (with:contract-address ["0x123"] *contract-address*) => "0x123")

^{:refer rt.solidity.compile-common/with:gas-limit :added "4.0"}
(fact "sets the gas limit"
  (with:gas-limit [100] *gas-limit*) => 100)

^{:refer rt.solidity.compile-common/with:params :added "4.0"}
(fact "overrides all parameters"
  (with:params {:temp true :gas-limit 100}
    [*temp* *gas-limit*])
  => [true 100])

^{:refer rt.solidity.compile-common/get-rt-settings :added "4.0"}
(fact "gets saves rt settings"
  (set-rt-settings "id" {:a 1})
  (get-rt-settings "id") => {:a 1})

^{:refer rt.solidity.compile-common/set-rt-settings :added "4.0"}
(fact "sets rt settings"
  (set-rt-settings "id" {:a 1})
  (get-rt-settings "id") => {:a 1})

^{:refer rt.solidity.compile-common/update-rt-settings :added "4.0"}
(fact "updates rt settings"
  (set-rt-settings "id" {:a 1})
  (update-rt-settings "id" {:b 2})
  (get-rt-settings "id") => {:a 1 :b 2})

^{:refer rt.solidity.compile-common/get-caller-address :added "4.0"}
(fact "gets the caller address"
  (set-rt-settings "id" {:caller-address "addr"})
  (get-caller-address "id") => "addr")

^{:refer rt.solidity.compile-common/get-caller-private-key :added "4.0"}
(fact "gets the caller private key"
  (set-rt-settings "id" {:caller-private-key "key"})
  (get-caller-private-key "id") => "key")

^{:refer rt.solidity.compile-common/get-contract-address :added "4.0"}
(fact "gets the contract address"
  (set-rt-settings "id" {:contract-address "addr"})
  (get-contract-address "id") => "addr")

^{:refer rt.solidity.compile-common/get-url :added "4.0"}
(fact "get sthe url for a rt"
  (with-redefs [l/rt (fn [& _] {:url "url"})]
    (get-url nil) => "url"))
