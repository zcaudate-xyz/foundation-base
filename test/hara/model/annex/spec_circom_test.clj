tahto/model/annex/spec_circom_test.clj:1:(ns tahto.model.annex.spec-circom-test
  (:use code.test)
  (:require [clojure.string :as str]
            [tahto.core.impl :as impl]
tahto/model/annex/spec_circom_test.clj:5:            [tahto.model.annex.spec-circom :refer :all]))

tahto/model/annex/spec_circom_test.clj:7:^{:refer tahto.model.annex.spec-circom/format-string :added "4.1"}
(fact "returns readable output for strings, symbols, and other values"
  (format-string "hello")
  => "\"hello\""

  (format-string 42)
  => 42

  (format-string 'x)
  => "x")

tahto/model/annex/spec_circom_test.clj:18:^{:refer tahto.model.annex.spec-circom/tf-template :added "4.1"}
(fact "transforms template definition to circom template syntax"
  (impl/emit-script '(template Multiplier [a b]
                       (signal input a)
                       (signal input b))
                    {:lang :circom})
  => string?)

tahto/model/annex/spec_circom_test.clj:26:^{:refer tahto.model.annex.spec-circom/tf-component :added "4.1"}
(fact "transforms component instantiation"
  (first (tf-component '(component c (MyTemplate 1 2))))
  => :%)

tahto/model/annex/spec_circom_test.clj:31:^{:refer tahto.model.annex.spec-circom/tf-signal :added "4.1"}
(fact "transforms signal declarations"
  (impl/emit-script '(signal input x) {:lang :circom})
  => "signal input x;"

  (impl/emit-script '(signal output y) {:lang :circom})
  => "signal output y;"

  (impl/emit-script '(signal z) {:lang :circom})
  => "signal z;")

tahto/model/annex/spec_circom_test.clj:42:^{:refer tahto.model.annex.spec-circom/tf-signal :added "4.1"}
(fact "transforms signal declarations"
  (impl/emit-script '(signal input x) {:lang :circom})
  => "signal input x;"

  (impl/emit-script '(signal output y) {:lang :circom})
  => "signal output y;"

  (impl/emit-script '(signal z) {:lang :circom})
  => "signal z;")

tahto/model/annex/spec_circom_test.clj:53:^{:refer tahto.model.annex.spec-circom/tf-var :added "4.1"}
(fact "transforms var declarations to circom var syntax"
  (impl/emit-script '(var x 10) {:lang :circom})
  => "var x = 10;")

tahto/model/annex/spec_circom_test.clj:58:^{:refer tahto.model.annex.spec-circom/tf-pragma :added "4.1"}
(fact "transforms pragma to circom pragma syntax"
  (impl/emit-script '(pragma circom |2.0.0|) {:lang :circom})
  => "pragma circom 2.0.0;")

tahto/model/annex/spec_circom_test.clj:63:^{:refer tahto.model.annex.spec-circom/tf-include :added "4.1"}
(fact "transforms include to circom include syntax"
  (impl/emit-script '(include "lib.circom") {:lang :circom})
  => "include \"lib.circom\";")

tahto/model/annex/spec_circom_test.clj:68:^{:refer tahto.model.annex.spec-circom/tf-main :added "4.1"}
(fact "transforms main component definition"
  (first (tf-main '(main {} (Multiplier))))
  => :%

  (let [form (tf-main '(main {} (Multiplier)))]
    (str/includes? (str form) "main"))
  => true)

tahto/model/annex/spec_circom_test.clj:77:^{:refer tahto.model.annex.spec-circom/tf-constraint :added "4.1"}
(fact "transforms constraints to circom constraint syntax"
  (first (tf-constraint '(<== c (+ a b))))
  => :%

  (let [form (tf-constraint '(<== c (+ a b)))]
    (str/includes? (str form) "<=="))
  => true)

tahto/model/annex/spec_circom_test.clj:86:^{:refer tahto.model.annex.spec-circom/tf-for :added "4.1"}
(fact "transforms for loop to circom for syntax"
  (let [form (tf-for '(for [i 0 10] (var x i)))]
    (str/includes? (str form) "for"))
  => true)
